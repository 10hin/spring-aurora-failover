terraform {
  required_version = "~> 1"
  required_providers {
    aws = {
        source = "hashicorp/aws"
        version = "~> 5"
    }
  }
}

provider "aws" {
  default_tags {
    tags = {
      Purpose = "AuroraFailover"
    }
  }
}

data "aws_partition" "_" {}
data "aws_region" "_" {}
data "aws_caller_identity" "_" {}
data "aws_availability_zones" "available" {
  state = "available"
  filter {
    name = "zone-type"
    values = ["availability-zone"]
  }
}

locals {
  aws_partition = data.aws_partition._.partition
  aws_region = data.aws_region._.name
  aws_account_id = data.aws_caller_identity._.account_id
  aws_azs = sort(data.aws_availability_zones.available.names)
  aws_az_ids = [
    for az in local.aws_azs:
    data.aws_availability_zones.available.zone_ids[index(data.aws_availability_zones.available.names, az)]
  ]
}

locals {
  project_name = "spring-aurora-failover"
}

data "aws_default_tags" "_" {}

data "aws_vpc" "_" {
  filter {
    name = "tag:Purpose"
    values = [data.aws_default_tags._.tags["Purpose"]]
  }
}

data "aws_subnets" "private" {
  filter {
    name = "vpc-id"
    values = [data.aws_vpc._.id]
  }
  filter {
    name = "tag:Purpose"
    values = [data.aws_default_tags._.tags["Purpose"]]
  }
  filter {
    name = "tag:kubernetes.io/role/internal-elb"
    values = ["1"]
  }
}

data "aws_rds_engine_version" "main" {
  engine = "aurora-mysql"
  default_only = true
  latest = true
}

resource "aws_db_subnet_group" "main" {
  name = local.project_name
  subnet_ids = data.aws_subnets.private.ids
}

resource "aws_rds_cluster_parameter_group" "main" {
  name = "${local.project_name}-cpg"
  family = data.aws_rds_engine_version.main.parameter_group_family
}

resource "aws_db_parameter_group" "main" {
  name = "${local.project_name}-pg"
  family = data.aws_rds_engine_version.main.parameter_group_family
}

resource "aws_kms_alias" "main_master_user_secret" {
  name = "alias/${local.project_name}-aurora-master-user-credentials"
  target_key_id = aws_kms_key.main_master_user_secret.arn
}
resource "aws_kms_key" "main_master_user_secret" {
  description = "aurora master user credentials for ${local.project_name}"
  deletion_window_in_days = 7
  policy = data.aws_iam_policy_document.main_master_user_secret.json
}
data "aws_iam_policy_document" "main_master_user_secret" {
  statement {
    principals {
      type = "AWS"
      identifiers = ["arn:${local.aws_partition}:iam::${local.aws_account_id}:root"]
    }
    actions = ["kms:*"]
    resources = ["*"]
  }
}

resource "aws_rds_cluster" "main" {
  # identity
  cluster_identifier = local.project_name
  copy_tags_to_snapshot = true

  # networking
  availability_zones = local.aws_azs
  db_subnet_group_name = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.aurora.id]

  # db version
  engine = "aurora-mysql"
  engine_version = data.aws_rds_engine_version.main.version_actual

  # db configuration
  db_cluster_parameter_group_name = aws_rds_cluster_parameter_group.main.name
  master_username = "root"
  manage_master_user_password = true
  master_user_secret_kms_key_id = aws_kms_key.main_master_user_secret.arn

  # database deletion
  deletion_protection = false
  skip_final_snapshot = true
}

resource "aws_security_group" "aurora" {
  name = "${local.project_name}-aurora"
  vpc_id = data.aws_vpc._.id
}

# resource "aws_rds_cluster_instance" "main" {
#   count = 2

#   # identity
#   cluster_identifier = aws_rds_cluster.main.cluster_identifier
#   identifier = "${local.project_name}-${format("%02d", count.index)}"
#   copy_tags_to_snapshot = true

#   # networking
#   publicly_accessible = false
#   db_subnet_group_name = aws_db_subnet_group.main.name

#   # instance resource
#   instance_class = "db.t4g.medium"

#   # db version
#   engine = aws_rds_cluster.main.engine
#   engine_version = aws_rds_cluster.main.engine_version

#   # db configuration
#   db_parameter_group_name = aws_db_parameter_group.main.name

# }
