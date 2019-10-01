java_library(
    name = "client",
    srcs = ["ObfuscatorClient.java"],
    visibility = ["//src/main/java/com/example/helloworld:__subpackages__"],
    runtime_deps = [
        "@maven//:io_opencensus_opencensus_contrib_agent",
        "@maven//:io_opencensus_opencensus_impl",
    ],
    deps = [
        "//src/main/proto/obfuscator:lib",
        "//src/main/proto/obfuscator:obfuscator_java_proto",
        "//src/main/proto/obfuscator:obfuscator_proto",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//stub",
        "@maven//:io_opencensus_opencensus_contrib_grpc_metrics",
    ],
)

java_binary(
    name = "ObfuscatorServer",
    srcs = ["ObfuscatorServer.java"],
    runtime_deps = [
        "@maven//:io_opencensus_opencensus_contrib_agent",
        "@maven//:io_opencensus_opencensus_impl",
    ],
    deps = [
        "//src/main/proto/obfuscator:lib",
        "//src/main/proto/obfuscator:obfuscator_java_proto",
        "//src/main/proto/obfuscator:obfuscator_proto",
        "@io_grpc_grpc_java//core",
        "@io_grpc_grpc_java//stub",
        "@io_grpc_grpc_java//netty",
        "@maven//:com_google_guava_guava",
        "@maven//:io_opencensus_opencensus_api",
        "@maven//:io_opencensus_opencensus_contrib_grpc_metrics",
        "@maven//:io_opencensus_opencensus_contrib_http_servlet",
        "@maven//:io_opencensus_opencensus_contrib_http_util",
        "@maven//:io_opencensus_opencensus_exporter_stats_prometheus",
        "@maven//:io_opencensus_opencensus_exporter_trace_jaeger",
        "@maven//:io_prometheus_simpleclient_httpserver",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
