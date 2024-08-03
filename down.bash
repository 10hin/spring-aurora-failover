#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

set +e
terraform -chdir=infrastructures/terraform/ plan -destroy -detailed-exitcode &> /dev/null
destroy_plan_exit_code="$?"
set -e

if [[ "${destroy_plan_exit_code}" = '2' ]]
then
  terraform -chdir=infrastructures/terraform/ plan -destroy -out destroy.plan.out
  terraform -chdir=infrastructures/terraform/ apply destroy.plan.out
fi

if eksctl get cluster -f infrastructures/eksctl/cluster.yaml &> /dev/null
then
  eksctl delete cluster -w -f infrastructures/eksctl/cluster.yaml
fi
