FROM openjdk:8u222-jre

# Expose server ports.
EXPOSE 2019
EXPOSE 9090

COPY scratch/entrypoint.sh /entrypoint
COPY scratch/ObfuscatorServer /ObfuscatorServer
COPY scratch/ObfuscatorServer.runfiles /ObfuscatorServer.runfiles
RUN rm -rf /ObfuscatorServer.runfiles/local_jdk && ln -sf /usr/local/openjdk-8 /ObfuscatorServer.runfiles/local_jdk

ENTRYPOINT ["/entrypoint"]
