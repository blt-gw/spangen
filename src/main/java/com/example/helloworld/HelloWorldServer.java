package com.example.helloworld;

import com.example.obfuscator.ObfuscatorClient;
import com.example.obfuscator.ReverseResponse;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.contrib.http.servlet.OcHttpServletFilter;
import io.opencensus.contrib.http.util.HttpViews;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.trace.jaeger.JaegerExporterConfiguration;
import io.opencensus.exporter.trace.jaeger.JaegerTraceExporter;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import io.prometheus.client.exporter.HTTPServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static io.opencensus.contrib.http.servlet.OcHttpServletFilter.OC_TRACE_PROPAGATOR;

/**
 * Sample application that shows how to instrument jetty server.
 */
public class HelloWorldServer extends AbstractHandler {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private static void initStatsExporter() throws IOException {
        HttpViews.registerAllServerViews();
        RpcViews.registerAllGrpcViews();

        // Register Prometheus exporters and export metrics to a Prometheus HTTPServer.
        // Refer to https://prometheus.io/ to run Prometheus Server.
        PrometheusStatsCollector.createAndRegister();
        HTTPServer prometheusServer = new HTTPServer(9090, true);
    }

    private static void initTracing(String endpoint, String serviceName) {
        TraceConfig traceConfig = Tracing.getTraceConfig();

        // default sampler is set to Samplers.alwaysSample() for demonstration. In production
        // or in high QPS environment please use default sampler.
        traceConfig.updateActiveTraceParams(
            traceConfig.getActiveTraceParams().toBuilder().setSampler(Samplers.probabilitySampler(0.01)).build());

        // Register Jaeger Tracing. Refer to https://www.jaegertracing.io/docs/1.8/getting-started/ to
        // run Jaeger
        JaegerTraceExporter.createAndRegister(
            JaegerExporterConfiguration.builder()
                                       .setThriftEndpoint(endpoint)
                                       .setServiceName(serviceName)
                                       .build()
        );
    }

    public static void main(String[] args) throws Exception {
        File confFile = new File(args[0]);
        Preconditions.checkArgument(confFile.canRead(), "unable to read configuration file at given path");
        Config conf = ConfigFactory.parseFile(confFile);

        initTracing(conf.getString("jaeger.thrift_endpoint"), conf.getString("jaeger.service_name"));
        initStatsExporter();

        Server server = new Server(8080);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.setInitParameter("obfuscator_host", conf.getString("obfuscator.host"));
        contextHandler.setInitParameter("obfuscator_port", conf.getString("obfuscator.port"));

        contextHandler.addServlet(HelloServlet.class, "/");

        contextHandler.getServletHandler().addFilterWithMapping(
            OcHttpServletFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        contextHandler.setAttribute(OC_TRACE_PROPAGATOR, Tracing.getPropagationComponent().getB3Format());

        server.setHandler(contextHandler);

        server.start();
        server.join();
    }

    @Override
    public void handle(
        String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response
    )
        throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Hello World. default handler.</h1>");
    }

    public static class HelloServlet extends HttpServlet {
        private ObfuscatorClient client;

        public void init(ServletConfig config) throws ServletException {
            super.init(config);
            String obfuscatorHost = getServletContext().getInitParameter("obfuscator_host");
            int obfuscatorPort = Integer.parseInt(getServletContext().getInitParameter("obfuscator_port"));
            this.client = ObfuscatorClient.builder()
                                          .setServerHost(obfuscatorHost)
                                          .setServerPort(obfuscatorPort)
                                          .build();
        }

        protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
        ) throws IOException {
            AsyncContext async = request.startAsync();
            ServletOutputStream out = response.getOutputStream();
            ObfuscatorClient client = this.client;
            io.grpc.Context context = io.grpc.Context.current();
            out.setWriteListener(new WriteListener() {
                @Override
                public void onWritePossible() throws IOException {
                    Context previousContext = context.attach();
                    try {
                        response.setHeader("content-type", "application/json");
                        JsonFactory jfactory = new JsonFactory();
                        JsonGenerator jGenerator = jfactory.createGenerator(out, JsonEncoding.UTF8);
                        jGenerator.writeStartObject();
                        jGenerator.writeFieldName("characters");
                        jGenerator.writeStartArray();

                        final CountDownLatch streamAlive = new CountDownLatch(1);

                        StreamObserver<ReverseResponse> streamObserver = new StreamObserver<ReverseResponse>() {
                            @Override
                            public void onNext(final ReverseResponse value) {
                                assert (out.isReady());
                                try {
                                    jGenerator.writeString(value.getChar());
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }

                            @Override
                            public void onError(final Throwable t) {
                                getServletContext().log("Async Error", t);
                            }

                            @Override
                            public void onCompleted() {
                                streamAlive.countDown();
                            }
                        };
                        client.reverse("hello, world", streamObserver);

                        try {
                            streamAlive.await();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        jGenerator.writeEndArray();
                        jGenerator.writeEndObject();
                        jGenerator.close();
                        response.setStatus(200);
                        async.complete();
                    } finally {
                        context.detach(previousContext);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    async.complete();
                }
            });
        }
    }
}
