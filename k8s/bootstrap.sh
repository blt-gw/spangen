#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

bazel build //...

minikube start --memory=16384 --cpus=4 -p spangen
minikube -p spangen ssh "sudo ip link set docker0 promisc on"
eval $(minikube docker-env -p spangen)

pushd ${__dir}

images/build.sh

pushd manifests
kubectl apply --filename basics.yml
kubectl apply --filename jaeger-operator/
kubectl apply --filename jaeger/
istio/install.sh
sleep 60
kubectl apply --filename spangen.yml
popd

popd
