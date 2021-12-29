### RSocket-requests-lease

Simplistic example for https://jauntsdn.com/post/rsocket-lease-concurrency-limiting/ - 
latency based service concurrency control by RSocket requests lease.
  
Application consists of single RSocket-RPC client connecting set of RSocket-RPC servers via reverse proxy.

1. Servers   
`com/jauntsdn/rsocket/showcase/lease/server/Main.java`  

2. Proxy
`com/jauntsdn/rsocket/showcase/lease/proxy/Main.java`

3. Client
`com/jauntsdn/rsocket/showcase/lease/client/Main.java`

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
