# Spangen

Demonstration project to show that spans are disconnected when bridging Jetty HTTP -> gRPC.

## Steps to Reproduce

To reproduce this issue we need to start three pieces of software. The first is a local jaeger:

```
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.8
```

This command will start a localhost jaeger, the UI of which you can access at http://localhost:16686. More details [here](https://www.jaegertracing.io/docs/1.8/getting-started/#all-in-one).

Start `obfuscator`, the project gRPC service whose protocol is defined in [src/main/proto/obfuscator/obfuscator.proto]:

```
./bin/run_obfuscator
```

This will start the server on port 2019, listening for local connections. Now start `helloworld`, an jetty server that
sends the string "hello, world" to `obfuscator` to be reversed, prior to rolling it up into json:

```
./bin/run_helloworld
```

The server `helloworld` will be running on 8080. You can confirm that the whole setup works like so:

```
> curl -vv http://localhost:8080
* Rebuilt URL to: http://localhost:8080/
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET / HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Date: Tue, 01 Oct 2019 00:56:53 GMT
< Content-Type: application/json
< Content-Length: 64
< Server: Jetty(9.4.18.v20190429)
<
* Connection #0 to host localhost left intact
{"characters":["d","l","r","o","w"," ",",","o","l","l","e","h"]}
```

Both `helloworld` and `obfuscator` are configured to trace all requests, so we should be able to go Jaeger and see the 
trace span both servers. When I query Jaeger I find that there is one trace for `helloworld: /` and another for 
`helloworldserver: Sent.com.example.obfuscator.Obfuscator.Reverse`, each having distinct trace ids.  
