FROM alpine:3.10

RUN apk update  && apk add wrk

COPY scratch/entrypoint.sh /entrypoint
ENTRYPOINT ["/entrypoint"]
