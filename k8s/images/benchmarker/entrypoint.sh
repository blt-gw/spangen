#!/usr/bin/env sh

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

wrk --connections=10 --threads=1 --duration=1h http://helloworld.spangen:8080
