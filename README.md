# Spring Aurora Failover

## Setup

```
eksctl create cluster -f infrastructures/eksctl/cluster.yaml
cd infrastructures/terraform
terraform init
terraform plan -out plan.out
terraform apply plan.out
```
