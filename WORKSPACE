load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# =========================
#  gRPC Building
# =========================
http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "8b495f58aaf75138b24775600a062bbdaa754d85f7ab2a47b2c9ecb432836dd1",
    strip_prefix = "grpc-java-1.24.0",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.24.0.tar.gz"],
)

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS")

grpc_java_repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

# =========================
#  Maven Dependencies
# =========================
RULES_JVM_EXTERNAL_TAG = "2.8"
RULES_JVM_EXTERNAL_SHA = "79c9850690d7614ecdb72d68394f994fef7534b292c4867ce5e7dec0aa7bdfad"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

OPENCENSUS_VERSION = "0.24.0"

JACKSON_DEFAULT_VERSION = "2.9.10"

maven_install(
    # WARNING
    # We pin artifacts, which makes builds fast but does add an extra complication.
    # Once you've added a dep here you _must_ run the following on the command line:
    #
    # > bazel run @unpinned_maven//:pin
    artifacts = [
        "com.fasterxml.jackson.core:jackson-core:" + JACKSON_DEFAULT_VERSION,
        "com.fasterxml.jackson.core:jackson-databind:" + JACKSON_DEFAULT_VERSION,
        "com.google.guava:guava:28.0-jre",
        "io.opencensus:opencensus-api:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-agent:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-grpc-metrics:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-exporter-stats-stackdriver:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-exporter-stats-prometheus:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-exporter-trace-stackdriver:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-exporter-trace-jaeger:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-impl:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-http-servlet:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-http-util:" + OPENCENSUS_VERSION,
        "io.opencensus:opencensus-contrib-http-jetty-client:" + OPENCENSUS_VERSION,
        "io.prometheus:simpleclient_httpserver:0.3.0",
        "org.slf4j:slf4j-api:1.7.25",
        "org.eclipse.jetty:jetty-server:9.4.18.v20190429",
        "org.eclipse.jetty:jetty-servlet:9.4.18.v20190429",
        "org.eclipse.jetty:jetty-util:9.4.18.v20190429",
        "javax.servlet:javax.servlet-api:3.1.0",
        "org.apache.logging.log4j:log4j-slf4j-impl:2.12.1",
        "org.apache.logging.log4j:log4j-core:2.12.1",
        "com.typesafe:config:1.3.4",
    ],
    override_targets = IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS,
    fetch_sources = True,
    maven_install_json = "//:maven_install.json",
    repositories = [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://nexus.bedatadriven.com/content/groups/public/",
    ],
)

load("@maven//:defs.bzl", "pinned_maven_install")
pinned_maven_install()
