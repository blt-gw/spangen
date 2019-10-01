load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "obfuscator_proto",
    visibility = ["//src/main/java/com/example:__subpackages__"],
    srcs = ["obfuscator.proto"],
)

java_proto_library(
    name = "obfuscator_java_proto",
    visibility = ["//src/main/java/com/example:__subpackages__"],
    deps = [
        ":obfuscator_proto",
    ],
)

java_grpc_library(
    name = "lib",
    srcs = [":obfuscator_proto"],
    visibility = ["//src/main/java/com/example:__subpackages__"],
    deps = [
        ":obfuscator_java_proto",
    ],
)
