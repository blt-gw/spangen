#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -o xtrace

__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

pushd ${__dir}

if [[ ! -d istio-1.3.1 ]]; then
    curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.3.1 sh -
fi

pushd istio-1.3.1

helm template install/kubernetes/helm/istio-init --name istio-init --namespace istio-system | kubectl apply -f -
sleep 60
# See https://istio.io/docs/reference/config/installation-options/ for details on the flags we set here.
helm template install/kubernetes/helm/istio \
  --set global.mtls.enabled=false \
  --set sidecarInjectorWebhook.rewriteAppHTTPProbe=true \
  --set global.tracer.zipkin.address=jaeger-collector.observability:9411 \
  --set prometheus.enabled=true \
  --name istio --namespace istio-system | kubectl apply -f -

popd
popd
