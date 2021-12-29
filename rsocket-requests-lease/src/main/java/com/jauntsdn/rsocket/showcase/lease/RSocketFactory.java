package com.jauntsdn.rsocket.showcase.lease;

import com.jauntsdn.rsocket.*;
import java.net.InetSocketAddress;
import reactor.core.publisher.Mono;

public interface RSocketFactory {

  Mono<RSocket> client(InetSocketAddress address);

  Mono<RSocket> client(InetSocketAddress address, Lease.Configurer lease);

  Mono<Disposable> server(InetSocketAddress address, ServerAcceptor acceptor);

  Mono<Disposable> server(
      InetSocketAddress address, ServerStreamsAcceptor acceptor, Lease.Configurer lease);
}
