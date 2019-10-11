#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

AGENT=`find /ObfuscatorServer.runfiles -name 'opencensus-contrib-agent-*.jar' | head -n1`

/ObfuscatorServer --jvm_flag="-javaagent:${AGENT}" ${@}
