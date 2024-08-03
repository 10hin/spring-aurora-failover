#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

helm upgrade \
  --install \
  --namespace external-secrets \
  --create-namespace \
  --repo https://charts.external-secrets.io \
  external-secrets \
  external-secrets \
  --version 0.9.20 \
  --values values.yaml \
  ;
