#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

__root="$(git rev-parse --show-toplevel)"
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

pushd ${__dir}

rm -rf scratch || true
mkdir scratch || true

cp entrypoint.sh ./scratch/entrypoint.sh
chmod +x ./scratch/entrypoint.sh
chown -R $(whoami) scratch

docker build -t benchmarker:latest .

rm -rf scratch || true
popd
