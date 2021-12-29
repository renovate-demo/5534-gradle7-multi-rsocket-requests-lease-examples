package com.jauntsdn.rsocket.showcase.lease;

@javax.annotation.Generated(
    value = "jauntsdn.com rpc compiler (version 1.1.0)",
    comments = "source: com/jauntsdn/rsocket/showcase/lease/service.proto")
@com.jauntsdn.rsocket.Rpc.Generated(
    role = com.jauntsdn.rsocket.Rpc.Role.CLIENT,
    service = Service.class)
public final class ServiceClient implements Service {
  private final com.jauntsdn.rsocket.MessageStreams streams;
  private final io.netty.buffer.ByteBufAllocator allocator;
  private final java.util.function.Function<? super org.reactivestreams.Publisher<com.jauntsdn.rsocket.showcase.lease.Response>, ? extends org.reactivestreams.Publisher<com.jauntsdn.rsocket.showcase.lease.Response>> responseInstrumentation;
  private final com.jauntsdn.rsocket.Rpc.Codec rpcCodec;

  private ServiceClient(com.jauntsdn.rsocket.MessageStreams streams, java.util.Optional<com.jauntsdn.rsocket.RpcInstrumentation> instrumentation) {
    this.streams = streams;
    this.allocator = streams.allocator().orElse(io.netty.buffer.ByteBufAllocator.DEFAULT);
    com.jauntsdn.rsocket.RpcInstrumentation i = instrumentation == null
      ? streams.attributes().attr(com.jauntsdn.rsocket.Attributes.RPC_INSTRUMENTATION)
      : instrumentation.orElse(null);
    if (i == null) {
      this.responseInstrumentation = null;
    } else {
      this.responseInstrumentation = i.instrument("client", Service.SERVICE, Service.METHOD_RESPONSE, false);
    }
    com.jauntsdn.rsocket.Rpc.Codec codec = streams.attributes().attr(com.jauntsdn.rsocket.Attributes.RPC_CODEC);
    if (codec != null) {
      rpcCodec = codec;
      if (codec.isDisposable()) {
        streams.onClose().subscribe(ignored -> {}, err -> {}, () -> codec.dispose());
      }
      return;
    }
    throw new IllegalArgumentException("MessageStreams " + streams.getClass() + " does not provide RPC codec");
  }

  public static ServiceClient create(com.jauntsdn.rsocket.MessageStreams streams, java.util.Optional<com.jauntsdn.rsocket.RpcInstrumentation> instrumentation) {
    java.util.Objects.requireNonNull(streams, "streams");
    java.util.Objects.requireNonNull(instrumentation, "instrumentation");
    return new ServiceClient(streams, instrumentation);
  }

  public static ServiceClient create(com.jauntsdn.rsocket.MessageStreams streams) {
    java.util.Objects.requireNonNull(streams, "streams");
    return new ServiceClient(streams, null);
  }

  @com.jauntsdn.rsocket.Rpc.GeneratedMethod(returnType = com.jauntsdn.rsocket.showcase.lease.Response.class)
  public reactor.core.publisher.Mono<com.jauntsdn.rsocket.showcase.lease.Response> response(com.jauntsdn.rsocket.showcase.lease.Request message) {
    return response(message, io.netty.buffer.Unpooled.EMPTY_BUFFER);
  }

  @Override
  @com.jauntsdn.rsocket.Rpc.GeneratedMethod(returnType = com.jauntsdn.rsocket.showcase.lease.Response.class)
  public reactor.core.publisher.Mono<com.jauntsdn.rsocket.showcase.lease.Response> response(com.jauntsdn.rsocket.showcase.lease.Request message, io.netty.buffer.ByteBuf metadata) {
    reactor.core.publisher.Mono<com.jauntsdn.rsocket.showcase.lease.Response> response = reactor.core.publisher.Mono.defer(new java.util.function.Supplier<reactor.core.publisher.Mono<com.jauntsdn.rsocket.Message>>() {
      @Override
      public reactor.core.publisher.Mono<com.jauntsdn.rsocket.Message> get() {
        int externalMetadataSize = streams.attributes().intAttr(com.jauntsdn.rsocket.Attributes.EXTERNAL_METADATA_SIZE);
        int dataSize = message.getSerializedSize();
        int localHeader = com.jauntsdn.rsocket.MessageMetadata.header(metadata);
        boolean isDefaultService = com.jauntsdn.rsocket.MessageMetadata.defaultService(localHeader);
        String service = isDefaultService ? com.jauntsdn.rsocket.Rpc.RpcMetadata.defaultService() : Service.SERVICE;
        com.jauntsdn.rsocket.Rpc.Codec codec = rpcCodec;
        io.netty.buffer.ByteBuf content = codec.encodeContent(allocator, metadata, localHeader, service, Service.METHOD_RESPONSE, false, Service.METHOD_RESPONSE_IDEMPOTENT, dataSize, externalMetadataSize);
        encode(content, message);
        com.jauntsdn.rsocket.Message message = codec.encodeMessage(content, Service.METHOD_RESPONSE_RANK);
        return streams.requestResponse(message);
      }
    }).map(decode(com.jauntsdn.rsocket.showcase.lease.Response.parser()));
    if (responseInstrumentation != null) {
      return response.transform(responseInstrumentation);
    }
    return response;
  }

  private io.netty.buffer.ByteBuf encode(io.netty.buffer.ByteBuf content, final com.google.protobuf.MessageLite message) {
    int length = message.getSerializedSize();
    try {
      int writerIndex = content.writerIndex();
      message.writeTo(com.google.protobuf.CodedOutputStream.newInstance(content.internalNioBuffer(writerIndex, length)));
      content.writerIndex(writerIndex + length);
      return content;
    } catch (Throwable t) {
      content.release();
      com.jauntsdn.rsocket.exceptions.Exceptions.throwIfJvmFatal(t);
      throw new com.jauntsdn.rsocket.exceptions.SerializationException("ServiceClient: message serialization error", t);
    }
  }

  private static <T> java.util.function.Function<com.jauntsdn.rsocket.Message, T> decode(final com.google.protobuf.Parser<T> parser) {
    return new java.util.function.Function<com.jauntsdn.rsocket.Message, T>() {
      @Override
      public T apply(com.jauntsdn.rsocket.Message message) {
        try {
          io.netty.buffer.ByteBuf messageData = message.data();
          com.google.protobuf.CodedInputStream is = com.google.protobuf.CodedInputStream.newInstance(messageData.internalNioBuffer(0, messageData.readableBytes()));
          return parser.parseFrom(is);
        } catch (Throwable t) {
          com.jauntsdn.rsocket.exceptions.Exceptions.throwIfJvmFatal(t);
          throw new com.jauntsdn.rsocket.exceptions.SerializationException("ServiceClient: message deserialization error", t);
        } finally {
          message.release();
        }
      }
    };
  }
}
