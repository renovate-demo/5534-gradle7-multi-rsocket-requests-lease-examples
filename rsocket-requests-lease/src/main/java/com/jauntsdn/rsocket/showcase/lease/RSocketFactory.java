package com.jauntsdn.rsocket.showcase.lease;

import com.jauntsdn.rsocket.*;
import java.net.InetSocketAddress;
import reactor.core.publisher.Mono;

public interface RSocketFactory {

  Mono<RSocket> proxyClient(InetSocketAddress address, Lease.Configurer lease);

  Mono<Disposable> proxyServer(InetSocketAddress address, ServerAcceptor acceptor);

  Mono<MessageStreams> serviceClient(InetSocketAddress address);

  Mono<Disposable> serviceServer(
      InetSocketAddress address, ServerStreamsAcceptor acceptor, Lease.Configurer lease);
}
