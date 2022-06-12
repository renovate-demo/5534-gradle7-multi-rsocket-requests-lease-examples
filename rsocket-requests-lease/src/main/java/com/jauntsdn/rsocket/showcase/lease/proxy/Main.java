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

package com.jauntsdn.rsocket.showcase.lease.proxy;

import com.jauntsdn.rsocket.RSocket;
import com.jauntsdn.rsocket.SetupMessage;
import com.jauntsdn.rsocket.showcase.lease.RSocketFactory;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(RSocketFactory rSocketFactory) {
    String address = System.getProperty("ADDRESS", "localhost:8308");
    String backendAddresses =
        System.getProperty("SERVERS", "localhost:8309,localhost:8310,localhost:8311");

    logger.info("Proxy bind address {}", address);
    logger.info("Backend servers addresses {}", backendAddresses);

    Set<Mono<RSocket>> backendRSockets =
        addresses(backendAddresses)
            .map(
                backendAddress ->
                    rSocketFactory.proxyClient(
                        backendAddress,
                        /*enable lease, no stats are recorded*/
                        leaseController -> Optional.empty()))
            .collect(Collectors.toSet());

    RSocket leastLoadedBalancerRSocket = new LeastLoadedBalancerRSocket(backendRSockets);

    rSocketFactory
        .proxyServer(
            address(address),
            (SetupMessage setup, RSocket requesterRSocket) -> Mono.just(leastLoadedBalancerRSocket))
        .block()
        .onClose()
        .awaitUninterruptibly();
  }

  private static Stream<InetSocketAddress> addresses(String addresses) {
    return Arrays.stream(addresses.split(","))
        .map(
            hostPort -> {
              String[] hostAndPort = hostPort.split(":");
              String host = hostAndPort[0];
              int port = Integer.parseInt(hostAndPort[1]);
              return new InetSocketAddress(host, port);
            });
  }

  private static InetSocketAddress address(String address) {
    String[] hostPort = address.split(":");
    String host = hostPort[0];
    int port = Integer.parseInt(hostPort[1]);
    return new InetSocketAddress(host, port);
  }
}
