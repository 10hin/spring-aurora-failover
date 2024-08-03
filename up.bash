#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if ! eksctl get cluster -f infrastructures/eksctl/cluster.yaml &> /dev/null
then
  eksctl create cluster -f infrastructures/eksctl/cluster.yaml
fi

terraform -chdir=infrastructures/terraform/ init
terraform -chdir=infrastructures/terraform/ validate
terraform -chdir=infrastructures/terraform/ plan -out plan.out
terraform -chdir=infrastructures/terraform/ apply plan.out

./infrastructures/kubernetes/external-secrets/helm-install.bash

kubectl -n external-secrets rollout status deployment -l app.kubernetes.io/instance=external-secrets -w

kubectl apply -k ./infrastructures/kubernetes/external-secrets-resources/
kubectl apply -f ./infrastructures/kubernetes/standarddriver/
