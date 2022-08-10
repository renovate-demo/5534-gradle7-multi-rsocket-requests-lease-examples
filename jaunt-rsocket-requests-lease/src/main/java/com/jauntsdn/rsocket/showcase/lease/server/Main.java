/*
 * Copyright 2020 - present Maksym Ostroverkhov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jauntsdn.rsocket.showcase.lease.server;

import com.jauntsdn.rsocket.*;
import com.jauntsdn.rsocket.exceptions.RejectedException;
import com.jauntsdn.rsocket.showcase.lease.RSocketFactory;
import com.jauntsdn.rsocket.showcase.lease.ServiceServer;
import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.HdrHistogram.Recorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(RSocketFactory rSocketFactory) {
    Integer leaseAllowedRequests = Integer.getInteger("ALLOWED_REQUESTS", 200);
    String concurrencyDelay =
        System.getProperty("CONCURRENCY_DELAY", "10 => 2; 50 => 5; 120 => 20; => 5000");
    String address = System.getProperty("ADDRESS", "localhost:8309");

    logger.info("Server bind address is {}", address);
    logger.info("Lease allowed requests per second: {}", leaseAllowedRequests);

    InetSocketAddress inetSocketAddress = address(address);

    Duration leaseTimeToLive = Duration.ofSeconds(1);

    rSocketFactory
        .serviceServer(
            inetSocketAddress,
            /*acceptor*/
            (SetupMessage setup, MessageStreams messageStreams) ->
                Mono.just(
                    ServiceServer.create(new SaturableService(inetSocketAddress, concurrencyDelay))
                        .withLifecycle(messageStreams)),
            /*lease controller*/
            leaseController -> {
              ConstantLeaseController constantLeaseController =
                  new ConstantLeaseController(
                      leaseController, inetSocketAddress, leaseTimeToLive, leaseAllowedRequests);
              return Optional.of(constantLeaseController);
            })
        .block()
        .onClose()
        .awaitUninterruptibly();
  }

  private static class ConstantLeaseController implements Lease.StatsRecorder<String> {
    private static final String divider = repeat("=", 80);
    private final ServiceStatsRecorder statsRecorder;

    public ConstantLeaseController(
        Lease.Controller controller, InetSocketAddress address, Duration ttl, int allowedRequests) {
      ServiceStatsRecorder recorder = statsRecorder = new ServiceStatsRecorder();
      int ttlMillis = Math.min(Integer.MAX_VALUE, (int) ttl.toMillis());

      ScheduledFuture<?> sendLeaseFuture =
          controller
              .executor()
              .scheduleAtFixedRate(
                  () -> {
                    logger.info(divider);
                    ServiceStatsRecorder.Counters counters = recorder.counters();
                    int accepted = counters.acceptedRequests();
                    if (accepted > 0) {
                      logger.info("service {} accepted {} requests", address, accepted);
                    }
                    int rejected = counters.rejectedRequests();
                    if (rejected > 0) {
                      logger.info("service {} rejected {} requests", address, rejected);
                    }
                    Map<String, Long> latencies = recorder.latencies();
                    latencies.forEach(
                        (request, latency) ->
                            logger.info("service call {} latency is {} millis", request, latency));
                    logger.info(
                        "responder sends new lease, allowed requests is {}, time-to-live is {} millis",
                        allowedRequests,
                        ttlMillis);
                    controller.allow(ttlMillis, allowedRequests);
                  },
                  0,
                  ttlMillis,
                  TimeUnit.MILLISECONDS);

      controller.onClose().addListener(future -> sendLeaseFuture.cancel(true));
    }

    @Override
    public String onRequestStarted(Interaction.Type requestType, ByteBuf metadata) {
      return statsRecorder.onRequestStarted(requestType, metadata);
    }

    @Override
    public void onResponseStarted(
        Interaction.Type requestType,
        String request,
        Interaction.StreamSignal firstSignal,
        long latencyMicros) {
      statsRecorder.onResponseStarted(requestType, request, firstSignal, latencyMicros);
    }

    @Override
    public void onResponseTerminated(
        Interaction.Type requestType,
        String request,
        Interaction.StreamSignal lastSignal,
        long responseDurationMicros) {
      statsRecorder.onResponseTerminated(requestType, request, lastSignal, responseDurationMicros);
    }

    @Override
    public void onRtt(long rttMicros) {
      statsRecorder.onRtt(rttMicros);
    }

    @Override
    public void onError(Interaction.Type requestType, Throwable err) {
      statsRecorder.onError(requestType, err);
    }

    @Override
    public void onOpen() {}

    @Override
    public void onClose(long graceTimeoutMillis) {
      statsRecorder.onClose(graceTimeoutMillis);
    }
  }

  private static class ServiceStatsRecorder implements Lease.StatsRecorder<String> {
    private final AtomicInteger acceptedCounter = new AtomicInteger();
    private final AtomicInteger rejectedCounter = new AtomicInteger();
    private final ConcurrentMap<String, Recorder> histograms = new ConcurrentHashMap<>();

    public Counters counters() {
      int accepts = acceptedCounter.getAndSet(0);
      int rejects = rejectedCounter.getAndSet(0);

      return new Counters(accepts, rejects);
    }

    public Map<String, Long> latencies() {
      return histograms
          .entrySet()
          .stream()
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey,
                  entry -> {
                    long latencyMicros =
                        entry.getValue().getIntervalHistogram().getValueAtPercentile(99.0);
                    return TimeUnit.MICROSECONDS.toMillis(latencyMicros);
                  }));
    }

    @Override
    public String onRequestStarted(Interaction.Type requestType, ByteBuf metadata) {
      long header = Rpc.RpcMetadata.header(metadata);
      int flags = Rpc.RpcMetadata.flags(header);
      String service = Rpc.RpcMetadata.service(metadata, header, flags);
      String method = Rpc.RpcMetadata.method(metadata, header, flags);
      return String.format("%s/%s", service, method);
    }

    @Override
    public void onResponseStarted(
        Interaction.Type requestType,
        String request,
        Interaction.StreamSignal firstSignal,
        long latencyMicros) {
      Recorder recorder = histograms.computeIfAbsent(request, r -> new Recorder(15000000L, 3));
      recorder.recordValue(latencyMicros);
    }

    @Override
    public void onResponseTerminated(
        Interaction.Type requestType,
        String request,
        Interaction.StreamSignal lastSignal,
        long responseDurationMicros) {
      switch (lastSignal.type()) {
        case ON_ERROR:
          if (lastSignal.<Interaction.StreamSignal.Error>cast().value()
              instanceof RejectedException) {
            rejectedCounter.incrementAndGet();
          }
          break;
        case ON_COMPLETE:
          acceptedCounter.incrementAndGet();
          break;
      }
    }

    @Override
    public void onRtt(long rttMicros) {}

    @Override
    public void onError(Interaction.Type requestType, Throwable err) {}

    @Override
    public void onOpen() {}

    @Override
    public void onClose(long graceTimeoutMillis) {}

    static final class Counters {
      private final int accepted;
      private final int rejected;

      public Counters(int accepted, int rejected) {
        this.accepted = accepted;
        this.rejected = rejected;
      }

      public int acceptedRequests() {
        return accepted;
      }

      public int rejectedRequests() {
        return rejected;
      }
    }
  }

  private static InetSocketAddress address(String address) {
    String[] hostPort = address.split(":");
    String host = hostPort[0];
    int port = Integer.parseInt(hostPort[1]);
    return new InetSocketAddress(host, port);
  }

  private static String repeat(String str, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(str);
    }
    return sb.toString();
  }
}
