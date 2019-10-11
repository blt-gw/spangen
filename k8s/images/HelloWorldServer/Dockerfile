FROM openjdk:8u222-jre

# Expose server ports.
EXPOSE 8080
EXPOSE 9090

COPY scratch/entrypoint.sh /entrypoint
COPY scratch/HelloWorldServer /HelloWorldServer
COPY scratch/HelloWorldServer.runfiles /HelloWorldServer.runfiles
RUN rm -rf /HelloWorldServer.runfiles/local_jdk && ln -sf /usr/local/openjdk-8 /HelloWorldServer.runfiles/local_jdk

ENTRYPOINT ["/entrypoint"]
