#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

AGENT=`find /HelloWorldServer.runfiles -name 'opencensus-contrib-agent-*.jar' | head -n1`

/HelloWorldServer --jvm_flag="-javaagent:${AGENT}" ${@}
