package com.jauntsdn.rsocket.showcase.lease;

@javax.annotation.Generated(
    value = "jauntsdn.com rpc compiler (version 1.1.1)",
    comments = "source: com/jauntsdn/rsocket/showcase/lease/service.proto")
public interface Service {
  String SERVICE = "com.jauntsdn.rsocket.showcase.lease.Service";
  Class<?> SERVICE_TYPE = com.jauntsdn.rsocket.showcase.lease.Service.class;

  String METHOD_RESPONSE = "response";
  boolean METHOD_RESPONSE_IDEMPOTENT = false;
  int METHOD_RESPONSE_RANK = 0;

  reactor.core.publisher.Mono<com.jauntsdn.rsocket.showcase.lease.Response> response(com.jauntsdn.rsocket.showcase.lease.Request message, io.netty.buffer.ByteBuf metadata);
}
