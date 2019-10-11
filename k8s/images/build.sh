#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

pushd ${__dir}

./benchmarker/build_docker_images.sh
./HelloWorldServer/build_docker_images.sh
./obfuscator/build_docker_images.sh

popd
