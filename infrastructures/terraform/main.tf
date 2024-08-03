terraform {
  required_version = "~> 1"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
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
    name   = "zone-type"
    values = ["availability-zone"]
  }
}

locals {
  aws_partition  = data.aws_partition._.partition
  aws_region     = data.aws_region._.name
  aws_account_id = data.aws_caller_identity._.account_id
  aws_az_count   = 3
  aws_azs        = slice(sort(data.aws_availability_zones.available.names), 0, local.aws_az_count)
  aws_az_ids = [
    for az in local.aws_azs :
    data.aws_availability_zones.available.zone_ids[index(data.aws_availability_zones.available.names, az)]
  ]
}

locals {
  project_name = "spring-aurora-failover"
}

data "aws_default_tags" "_" {}

data "aws_eks_cluster" "_" {
  name = "aurora-failover"
}

data "aws_vpc" "_" {
  filter {
    name   = "tag:Purpose"
    values = [data.aws_default_tags._.tags["Purpose"]]
  }
}

locals {
  vpc_id = data.aws_vpc._.id
}

data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [local.vpc_id]
  }
  filter {
    name   = "tag:Purpose"
    values = [data.aws_default_tags._.tags["Purpose"]]
  }
  filter {
    name   = "tag:kubernetes.io/role/internal-elb"
    values = ["1"]
  }
}

data "aws_subnet" "private" {
  for_each = toset(data.aws_subnets.private.ids)

  id = each.key
}

locals {
  az_to_private_subnet_map = {
    for private_subnet in data.aws_subnet.private :
    private_subnet.availability_zone => private_subnet.id
  }
  private_subnet_ids = [
    for az in local.aws_azs :
    local.az_to_private_subnet_map[az]
  ]
}

locals {
  cluster_security_group_id = [
    for vpc_config in data.aws_eks_cluster._.vpc_config :
    vpc_config if vpc_config.vpc_id == local.vpc_id
  ][0].cluster_security_group_id
  additional_security_group_ids = toset([
    for vpc_config in data.aws_eks_cluster._.vpc_config :
    vpc_config if vpc_config.vpc_id == local.vpc_id
  ][0].security_group_ids)
  all_eks_cluster_security_group_ids = toset(flatten([
    local.cluster_security_group_id,
    local.additional_security_group_ids,
  ]))
}

data "aws_rds_engine_version" "main" {
  engine       = "aurora-mysql"
  default_only = true
  latest       = true
}

resource "aws_db_subnet_group" "main" {
  name       = local.project_name
  subnet_ids = local.private_subnet_ids
}

resource "aws_rds_cluster_parameter_group" "main" {
  name   = "${local.project_name}-cpg"
  family = data.aws_rds_engine_version.main.parameter_group_family
}

resource "aws_db_parameter_group" "main" {
  name   = "${local.project_name}-pg"
  family = data.aws_rds_engine_version.main.parameter_group_family
}

resource "aws_kms_alias" "main_master_user_secret" {
  name          = "alias/${local.project_name}-aurora-master-user-credentials"
  target_key_id = aws_kms_key.main_master_user_secret.arn
}
resource "aws_kms_key" "main_master_user_secret" {
  description             = "aurora master user credentials for ${local.project_name}"
  deletion_window_in_days = 7
  policy                  = data.aws_iam_policy_document.main_master_user_secret.json
}
data "aws_iam_policy_document" "main_master_user_secret" {
  statement {
    principals {
      type        = "AWS"
      identifiers = ["arn:${local.aws_partition}:iam::${local.aws_account_id}:root"]
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }
}

locals {
  master_username   = "root"
  mysql_ip_protocol = "tcp"
  mysql_tcp_port    = 3306
  database_name     = "spring_aurora_failover"
}

resource "aws_rds_cluster" "main" {
  # identity
  cluster_identifier    = local.project_name
  copy_tags_to_snapshot = true

  # networking
  availability_zones     = local.aws_azs
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.aurora.id]
  port                   = local.mysql_tcp_port

  # db version
  engine         = "aurora-mysql"
  engine_version = data.aws_rds_engine_version.main.version_actual

  # db configuration
  db_cluster_parameter_group_name = aws_rds_cluster_parameter_group.main.name
  master_username                 = local.master_username
  manage_master_user_password     = true
  master_user_secret_kms_key_id   = aws_kms_key.main_master_user_secret.arn
  database_name                   = local.database_name

  # database deletion
  deletion_protection = false
  skip_final_snapshot = true
}

resource "aws_security_group" "aurora" {
  name   = "${local.project_name}-aurora"
  vpc_id = local.vpc_id
}

resource "aws_vpc_security_group_ingress_rule" "proxy_to_aurora_ingress" {
  security_group_id = aws_security_group.aurora.id

  ip_protocol                  = local.mysql_ip_protocol
  from_port                    = local.mysql_tcp_port
  to_port                      = local.mysql_tcp_port
  referenced_security_group_id = aws_security_group.aurora.id
}
resource "aws_vpc_security_group_egress_rule" "proxy_to_aurora_egress" {
  security_group_id = aws_security_group.aurora.id

  ip_protocol                  = local.mysql_ip_protocol
  from_port                    = local.mysql_tcp_port
  to_port                      = local.mysql_tcp_port
  referenced_security_group_id = aws_security_group.aurora.id
}

resource "aws_vpc_security_group_ingress_rule" "pod_to_aurora" {
  for_each = local.all_eks_cluster_security_group_ids

  security_group_id            = aws_security_group.aurora.id
  ip_protocol                  = local.mysql_ip_protocol
  from_port                    = local.mysql_tcp_port
  to_port                      = local.mysql_tcp_port
  referenced_security_group_id = each.key
}

resource "aws_vpc_security_group_egress_rule" "pod_to_aurora" {
  for_each = local.all_eks_cluster_security_group_ids

  security_group_id            = each.key
  ip_protocol                  = local.mysql_ip_protocol
  from_port                    = local.mysql_tcp_port
  to_port                      = local.mysql_tcp_port
  referenced_security_group_id = aws_security_group.aurora.id
}

locals {
  aurora_instance_count = 3
}

resource "aws_rds_cluster_instance" "main" {
  for_each = {
    for idx in range(local.aurora_instance_count) :
    format("%02d", idx + 1) => local.aws_azs[idx % length(local.aws_azs)]
  }

  # identity
  cluster_identifier    = aws_rds_cluster.main.cluster_identifier
  identifier            = "${local.project_name}-${each.key}"
  copy_tags_to_snapshot = true

  # networking
  publicly_accessible  = false
  db_subnet_group_name = aws_db_subnet_group.main.name
  availability_zone    = each.value

  # instance resource
  instance_class = "db.t4g.medium"

  # db version
  engine         = aws_rds_cluster.main.engine
  engine_version = aws_rds_cluster.main.engine_version

  # db configuration
  db_parameter_group_name = aws_db_parameter_group.main.name

}

resource "aws_iam_role" "proxy" {
  name               = "${local.project_name}-proxy"
  assume_role_policy = data.aws_iam_policy_document.allow_proxy_to_assume.json
}

data "aws_iam_policy_document" "allow_proxy_to_assume" {
  statement {
    principals {
      type        = "Service"
      identifiers = ["rds.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role_policy" "allow_access_auth_secret" {
  role   = aws_iam_role.proxy.name
  name   = "allow-access-to-auth-secret"
  policy = data.aws_iam_policy_document.allow_access_auth_secret.json
}

data "aws_iam_policy_document" "allow_access_auth_secret" {
  statement {
    actions = [
      "secretsmanager:GetSecretValue",
    ]
    resources = [
      aws_rds_cluster.main.master_user_secret[0].secret_arn,
    ]
  }
  statement {
    actions = [
      "kms:Decrypt",
    ]
    resources = [
      aws_kms_key.main_master_user_secret.arn,
    ]
  }
}

resource "aws_db_proxy" "main" {
  name          = "${local.project_name}-proxy"
  debug_logging = false
  engine_family = "MYSQL"
  require_tls   = true
  role_arn      = aws_iam_role.proxy.arn

  vpc_subnet_ids         = local.private_subnet_ids
  vpc_security_group_ids = [aws_security_group.aurora.id]

  auth {
    auth_scheme = "SECRETS"
    iam_auth    = "DISABLED"
    secret_arn  = aws_rds_cluster.main.master_user_secret[0].secret_arn
  }

}

resource "aws_db_proxy_default_target_group" "main" {
  db_proxy_name = aws_db_proxy.main.name
}

resource "aws_db_proxy_target" "main" {
  db_proxy_name         = aws_db_proxy.main.name
  target_group_name     = aws_db_proxy_default_target_group.main.name
  db_cluster_identifier = aws_rds_cluster.main.cluster_identifier
}

resource "aws_db_proxy_endpoint" "main_reader" {
  db_proxy_name          = aws_db_proxy.main.name
  db_proxy_endpoint_name = "${aws_db_proxy.main.name}-ro"
  vpc_subnet_ids         = local.private_subnet_ids
  vpc_security_group_ids = [aws_security_group.aurora.id]
  target_role            = "READ_ONLY"
}

data "aws_iam_role" "irsa_external_secrets" {
  name = "aurora-spring-failover-eso"
}

resource "aws_iam_role_policy" "irsa_external_secrets_access_aurora_secret" {
  role   = data.aws_iam_role.irsa_external_secrets.name
  name   = "access-aurora-secret"
  policy = data.aws_iam_policy_document.allow_access_auth_secret.json
}

resource "local_file" "aurora_secret_clusterexternalsecret" {
  filename = "${path.root}/../kubernetes/external-secrets-resources/aurora-secret-clusterexternalsecret.yaml"
  content = templatefile("${path.root}/../kubernetes/external-secrets-resources/aurora-secret-clusterexternalsecret.yaml.tftpl", {
    secret_arn                     = aws_rds_cluster.main.master_user_secret[0].secret_arn
    aurora_cluster_endpoint        = aws_rds_cluster.main.endpoint
    aurora_cluster_reader_endpoint = aws_rds_cluster.main.reader_endpoint
    proxy_default_endpoint         = aws_db_proxy.main.endpoint
    proxy_reader_endpoint          = aws_db_proxy_endpoint.main_reader.endpoint
  })
}

resource "aws_iam_instance_profile" "bastion" {
  name = aws_iam_role.bastion.name
  role = aws_iam_role.bastion.name
}
resource "aws_iam_role" "bastion" {
  name               = "${local.project_name}-bastion"
  assume_role_policy = data.aws_iam_policy_document.assume_by_ec2.json
}
data "aws_iam_policy_document" "assume_by_ec2" {
  statement {
    principals {
      type = "Service"
      identifiers = [
        "ec2.amazonaws.com",
      ]
    }
    actions = [
      "sts:AssumeRole",
    ]
  }
}

resource "aws_iam_role_policy_attachment" "session_manager" {
  role       = aws_iam_role.bastion.name
  policy_arn = data.aws_iam_policy.AmazonSSMManagedInstanceCore.arn
}
data "aws_iam_policy" "AmazonSSMManagedInstanceCore" {
  name = "AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "access_aurora_secret" {
  role   = aws_iam_instance_profile.bastion.name
  policy = data.aws_iam_policy_document.allow_access_auth_secret.json
}

resource "aws_iam_role_policy" "access_init_resources" {
  role   = aws_iam_instance_profile.bastion.name
  policy = data.aws_iam_policy_document.allow_access_init_script.json
}

data "aws_ami" "al2023" {
  most_recent = true
  name_regex  = "^al2023-ami-\\d{4}.*-x86_64$"
  filter {
    name = "architecture"
    values = [
      "x86_64",
    ]
  }
  owners = [
    "137112412989", // aws
  ]
}

resource "aws_instance" "bastion" {
  ami                  = data.aws_ami.al2023.image_id
  instance_type        = "t2.nano"
  iam_instance_profile = aws_iam_instance_profile.bastion.name
  subnet_id            = lookup(local.az_to_private_subnet_map, local.aws_azs[0])
  vpc_security_group_ids = [
    aws_security_group.bastion.id,
  ]
  tags = {
    Name = "${local.project_name}-bastion"
  }
  user_data = <<-EOT
  #!/bin/bash
  set -euvx
  aws s3 cp 's3://${aws_s3_object.init_script.bucket}/${aws_s3_object.init_script.key}' /initdb.bash
  chmod 744 /initdb.bash
  bash /initdb.bash
  EOT
  depends_on = [
    aws_s3_object.init_script,
    aws_rds_cluster_instance.main,
  ]
}

resource "aws_s3_bucket" "init_resourecs" {
  bucket = "${local.project_name}-init-resources-${local.aws_account_id}"
}
resource "aws_s3_bucket_public_access_block" "init_resourced" {
  bucket = aws_s3_bucket.init_resourecs.bucket

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

locals {
  script_file_path = "${path.root}/initdb.bash"
}
resource "aws_s3_object" "init_script" {
  bucket = aws_s3_bucket.init_resourecs.bucket
  key    = "initdb.bash"
  content = templatefile("${local.script_file_path}.tftpl", {
    aurora_endpoint   = aws_rds_cluster.main.endpoint
    aurora_secret_arn = aws_rds_cluster.main.master_user_secret[0].secret_arn
    database_name     = local.database_name
    aurora_ddl        = file("${path.root}/../../applications/sql/ddl.sql")
  })
  source_hash = md5(templatefile("${local.script_file_path}.tftpl", {
    aurora_endpoint   = aws_rds_cluster.main.endpoint
    aurora_secret_arn = aws_rds_cluster.main.master_user_secret[0].secret_arn
    database_name     = local.database_name
    aurora_ddl        = file("${path.root}/../../applications/sql/ddl.sql")
  }))
}
data "aws_iam_policy_document" "allow_access_init_script" {
  statement {
    actions = [
      "s3:GetObject",
    ]
    resources = [
      aws_s3_object.init_script.arn,
    ]
  }
}

resource "aws_security_group" "bastion" {
  name   = "${local.project_name}-bastion"
  vpc_id = local.vpc_id
}
resource "aws_vpc_security_group_egress_rule" "bastion_to_aurora" {
  security_group_id = aws_security_group.bastion.id

  ip_protocol                  = local.mysql_ip_protocol
  from_port                    = local.mysql_tcp_port
  to_port                      = local.mysql_tcp_port
  referenced_security_group_id = aws_security_group.aurora.id
}
resource "aws_vpc_security_group_ingress_rule" "bastion_to_aurora" {
  security_group_id = aws_security_group.aurora.id

  ip_protocol                  = local.mysql_ip_protocol
  from_port                    = local.mysql_tcp_port
  to_port                      = local.mysql_tcp_port
  referenced_security_group_id = aws_security_group.bastion.id
}
resource "aws_vpc_security_group_egress_rule" "free_outbound" {
  security_group_id = aws_security_group.bastion.id

  ip_protocol = -1
  cidr_ipv4   = "0.0.0.0/0"
}
