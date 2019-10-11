# Spangen

Demonstration project to show that spans are disconnected when bridging Jetty HTTP -> gRPC in istio. 

## Steps to Reproduce

To reproduce this issue we need to boot the project minikube. You may do that by running the following:

```
./k8s/bootstrap.sh
```

This will create a new minikube on your system called `spangen`. You will need to have minikube set up for your environment
already. See instructions [here](https://kubernetes.io/docs/tasks/tools/install-minikube/) if you have not got minikube
installed. 

Once you have `spangen` minikube booted you will see three services deployed in the `spangen` namespace:

 * `obfuscator` : a gRPC service that reverses whatever you send it
 * `helloworld` : an HTTP service that sends 'hello, world' to `obfuscator` and then wraps the result up in json
 * `benchmarker` : a service to generate load against `helloworld`
 
All but the last service are sending traces directly to the jaeger collector running in `observability` namespace and,
being that they're embedded in an istio mesh, the mesh is sending traces to the collector as well. You can confirm 
that `helloworld` is working as expected by doing the following:


To examine the traces do the following:

```

```

This forwards the query port to your local machine, which you can then visit at http://localhost:16686. What you
_should_ see are traces connected from `helloworld -> obfuscator` with some mesh in between but this is not happening. 
When I examine the UI I see only a single, rootless trace apparently from `helloworld`. 

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

