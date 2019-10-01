package com.example.obfuscator;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class ObfuscatorClient {
    private int serverPort = 2019;
    private String serverHost = "localhost";

    private ManagedChannel channel;
    private ObfuscatorGrpc.ObfuscatorStub stub;

    public static Builder builder() {
        return new Builder();
    }

    public void reverse(String msg, StreamObserver<ReverseResponse> streamObserver) {
        ReverseRequest request = ReverseRequest.newBuilder().setMsg(msg).build();
        stub.reverse(request, streamObserver);
    }

    public void shutdown() throws InterruptedException {
        this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static class Builder {
        private ObfuscatorClient clientToBuild;

        Builder() {
            this.clientToBuild = new ObfuscatorClient();
        }

        public ObfuscatorClient build() {
            ObfuscatorClient builtClient = this.clientToBuild;
            builtClient.channel = ManagedChannelBuilder.forAddress(this.clientToBuild.serverHost,
                                                                   this.clientToBuild.serverPort)
                                                       .usePlaintext()
                                                       .build();
            builtClient.stub = ObfuscatorGrpc.newStub(builtClient.channel);
            this.clientToBuild = new ObfuscatorClient();
            return builtClient;
        }

        public Builder setServerPort(int port) {
            this.clientToBuild.serverPort = port;
            return this;
        }

        public Builder setServerHost(String host) {
            this.clientToBuild.serverHost = host;
            return this;
        }
    }
}
