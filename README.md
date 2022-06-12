![Message-Streams](readme/mstreams.png)

[![Build](https://github.com/jauntsdn/rsocket-requests-lease-examples/actions/workflows/ci-build.yml/badge.svg)](https://github.com/jauntsdn/rsocket-requests-lease-examples/actions/workflows/ci-build.yml)
### Service concurrency limiting with RSocket requests lease

This example demonstrates how to perform latency/stats based call concurrency limiting & circuit breaking
of Message-Streams/RSocket-RPC services with [RSocket requests leasing](https://jauntsdn.com/post/rsocket-lease-concurrency-limiting/).

Application consists of single RSocket-RPC client connecting set of RSocket-RPC servers via reverse proxy.

1. [Servers](https://github.com/jauntsdn/rsocket-requests-lease-examples/blob/feature/oss/rsocket-requests-lease/src/main/java/com/jauntsdn/rsocket/showcase/lease/server/Main.java) 
provide RPC [service](https://github.com/jauntsdn/rsocket-requests-lease-examples/blob/feature/oss/rsocket-requests-lease/src/main/java/com/jauntsdn/rsocket/showcase/lease/server/SaturableService.java) 
with saturation - its response latencies grow with number of concurrent requests. Also server has lease controller
which collects service response stats & allows constant # of requests on responder side, at fixed rate (rate limiting).

2. [Proxy](https://github.com/jauntsdn/rsocket-requests-lease-examples/blob/feature/oss/rsocket-requests-lease/src/main/java/com/jauntsdn/rsocket/showcase/lease/proxy/Main.java) 
routes requests from client to least loaded server using `RSocket.availability()` - which represents # of 
non-expired allowed requests on requester side: [0 ; 1].
It rejects requests if there are no servers ready to accept request (servers are absent, or all servers have 0 availability)

3. [Client](https://github.com/jauntsdn/rsocket-requests-lease-examples/blob/feature/oss/rsocket-requests-lease/src/main/java/com/jauntsdn/rsocket/showcase/lease/client/Main.java) 
sends RPC requests to `Proxy` then reports # of successful and failed responses, together with 
observed response latencies.

[RSocket-JVM runtimes](https://jauntsdn.com/post/rsocket-jvm/) are stripped on this branch.

## License
Copyright 2020 - Present Maksym Ostroverkhov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
