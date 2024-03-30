#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

eksctl create cluster -f infrastructures/eksctl/cluster.yaml

terraform -chdir infrastructures/terraform/ init
terraform -chdir infrastructures/terraform/ validate
terraform -chdir infrastructures/terraform/ plan -out plan.out
terraform -chdir infrastructures/terraform/ apply plan.out
