package com.example.obfuscator;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.trace.jaeger.JaegerExporterConfiguration;
import io.opencensus.exporter.trace.jaeger.JaegerTraceExporter;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import io.prometheus.client.exporter.HTTPServer;

import java.io.IOException;
import java.util.logging.Logger;

public class ObfuscatorServer {
    private static final Logger logger = Logger.getLogger(ObfuscatorServer.class.getName());
    private final Tracer tracer = Tracing.getTracer();
    private Server server;
    private HTTPServer prometheusServer;

    public static void main(String[] args) throws IOException, InterruptedException {
        ObfuscatorServer obfuscatorServer = new ObfuscatorServer();

        obfuscatorServer.start();
        obfuscatorServer.awaitTermination();
    }

    private void start() throws IOException, InterruptedException {
        RpcViews.registerAllGrpcViews();

        // Register Prometheus exporters and export metrics to a Prometheus HTTPServer.
        PrometheusStatsCollector.createAndRegister();
        prometheusServer = new HTTPServer(9090, true);

        TraceConfig traceConfig = Tracing.getTraceConfig();

        // default sampler is set to Samplers.alwaysSample() for demonstration. In production
        // or in high QPS environment please use default sampler.
        traceConfig.updateActiveTraceParams(
            traceConfig.getActiveTraceParams().toBuilder().setSampler(Samplers.probabilitySampler(0.01)).build());

        JaegerTraceExporter.createAndRegister(
            JaegerExporterConfiguration.builder()
                                       .setThriftEndpoint("http://jaeger-collector.observability:14268/api/traces")
                                       .setServiceName("ObfuscatorServer")
                                       .build()
        );

        server = ServerBuilder.forPort(2019).addService(new ObfuscatorImpl()).build();
        server.start();
        server.awaitTermination();
    }

    private void awaitTermination() throws InterruptedException {
        server.awaitTermination();
        prometheusServer.stop();
    }

    static class ObfuscatorImpl extends ObfuscatorGrpc.ObfuscatorImplBase {
        @Override
        public void reverse(ReverseRequest request, StreamObserver<ReverseResponse> streamObserver) {
            Span span = Tracing.getTracer().getCurrentSpan();
            try {
                span.putAttribute("input", AttributeValue.stringAttributeValue(request.getMsg()));
                span.putAttribute("input_length", AttributeValue.longAttributeValue(request.getMsg().length()));
                String reversed = new StringBuilder(request.getMsg()).reverse().toString();
                for (char c : reversed.toCharArray()) {
                    streamObserver.onNext(ReverseResponse.newBuilder().setChar(Character.toString(c)).build());
                }
            } catch (Exception ex) {
                span.setStatus(io.opencensus.trace.Status.INTERNAL.withDescription(ex.getMessage()));
                streamObserver.onError(ex);
            } finally {
                streamObserver.onCompleted();
            }
        }
    }
}
