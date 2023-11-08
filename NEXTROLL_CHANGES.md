# NextRoll Changes to this Repo

This doc describes the changes NextRoll made to this repo after forking it, and serves as a guide to how to use this repo.

We will follow [these instructions](https://github.com/privacysandbox/aggregation-service/blob/b5c9c0015220b4631e6a8ca1df9ea36e20c32dee/docs/aws-aggregation-service.md).

## Build the AMI

I elected to build the AMI using [these special instructions](https://github.com/privacysandbox/aggregation-service/blob/b5c9c0015220b4631e6a8ca1df9ea36e20c32dee/build-scripts/aws/README.md) because we need the AMI to be in us-west-2 where most of our infrastructure is, not us-east-1.

```
tfenv install 1.2.3;
tfenv use 1.2.3
```

```
cd ~/projects
git clone https://github.com/privacysandbox/aggregation-service
cd aggregation-service/build-scripts/aws/terraform
```

```
git mv main.tf_sample main.tf
git mv codebuild.auto.tfvars_sample codebuild.auto.tfvars
```

```
vi main.tf
# Configure it as follows:
  backend "s3" {
    bucket         = "adroll-tfstate"
    dynamodb_table = "TerraformLock"
    region         = "us-west-2"
    key            = "udp/aggregation-service/build-scripts/terraform.tfstate"
  }
  ```

```
vi codebuild.auto.tfvars
# Configure it as follows:
region = "us-west-2"
build_artifacts_output_bucket = "adroll-aggregation-service-build-artifacts"
github_personal_access_token = "<generate a token without any scopes/permissions>"
```

Do not commit your personal access token to the repo, otherwise GitHub will detect it and expire it. 

```
terraform init
terraform plan
```

```
Terraform will perform the following actions:

  # aws_codebuild_project.aggregation-service-artifacts-build will be created
  + resource "aws_codebuild_project" "aggregation-service-artifacts-build" {
      + arn                  = (known after apply)
      + badge_enabled        = false
      + badge_url            = (known after apply)
      + build_timeout        = 60
      + description          = "Build aggregation service AMI and jar artifacts"
      + encryption_key       = (known after apply)
      + id                   = (known after apply)
      + name                 = "aggregation-service-artifacts-build"
      + project_visibility   = "PRIVATE"
      + public_project_alias = (known after apply)
      + queued_timeout       = 60
      + service_role         = (known after apply)
      + source_version       = "v2.0.0"
      + tags_all             = (known after apply)

      + artifacts {
          + encryption_disabled    = false
          + override_artifact_name = false
          + type                   = "NO_ARTIFACTS"
        }

      + cache {
          + location = "adroll-test-sandbox-2/build-cache"
          + type     = "S3"
        }

      + environment {
          + compute_type                = "BUILD_GENERAL1_MEDIUM"
          + image                       = (known after apply)
          + image_pull_credentials_type = "SERVICE_ROLE"
          + privileged_mode             = true
          + type                        = "LINUX_CONTAINER"

          + environment_variable {
              + name  = "AWS_DEFAULT_REGION"
              + type  = "PLAINTEXT"
              + value = "us-west-2"
            }
          + environment_variable {
              + name  = "AWS_ACCOUNT_ID"
              + type  = "PLAINTEXT"
              + value = "771945457201"
            }
          + environment_variable {
              + name  = "PACKER_GITHUB_API_TOKEN"
              + type  = "PLAINTEXT"
              + value = "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"
            }
          + environment_variable {
              + name  = "JARS_PUBLISH_BUCKET"
              + type  = "PLAINTEXT"
              + value = "adroll-test-sandbox-2"
            }
          + environment_variable {
              + name  = "JARS_PUBLISH_BUCKET_PATH"
              + type  = "PLAINTEXT"
              + value = "aggregation-service"
            }
        }

      + source {
          + buildspec       = "build-scripts/aws/buildspec.yml"
          + git_clone_depth = 1
          + location        = "https://github.com/privacysandbox/aggregation-service"
          + type            = "GITHUB"
        }
    }

  # aws_codebuild_project.bazel_build_container will be created
  + resource "aws_codebuild_project" "bazel_build_container" {
      + arn                  = (known after apply)
      + badge_enabled        = false
      + badge_url            = (known after apply)
      + build_timeout        = 10
      + description          = "Build container for aggregation service build"
      + encryption_key       = (known after apply)
      + id                   = (known after apply)
      + name                 = "bazel-build-container"
      + project_visibility   = "PRIVATE"
      + public_project_alias = (known after apply)
      + queued_timeout       = 480
      + service_role         = (known after apply)
      + source_version       = "v2.0.0"
      + tags_all             = (known after apply)

      + artifacts {
          + encryption_disabled    = false
          + override_artifact_name = false
          + type                   = "NO_ARTIFACTS"
        }

      + environment {
          + compute_type                = "BUILD_GENERAL1_MEDIUM"
          + image                       = "aws/codebuild/standard:6.0"
          + image_pull_credentials_type = "CODEBUILD"
          + privileged_mode             = true
          + type                        = "LINUX_CONTAINER"

          + environment_variable {
              + name  = "AWS_DEFAULT_REGION"
              + type  = "PLAINTEXT"
              + value = "us-west-2"
            }
          + environment_variable {
              + name  = "AWS_ACCOUNT_ID"
              + type  = "PLAINTEXT"
              + value = "771945457201"
            }
          + environment_variable {
              + name  = "IMAGE_REPO_NAME"
              + type  = "PLAINTEXT"
              + value = "bazel-build-container"
            }
          + environment_variable {
              + name  = "IMAGE_TAG"
              + type  = "PLAINTEXT"
              + value = "2.0.0"
            }
        }

      + logs_config {

          + s3_logs {
              + encryption_disabled = false
              + location            = (known after apply)
              + status              = "ENABLED"
            }
        }

      + source {
          + buildspec       = "build-scripts/aws/build-container/buildspec.yml"
          + git_clone_depth = 1
          + location        = "https://github.com/privacysandbox/aggregation-service"
          + type            = "GITHUB"
        }
    }

  # aws_codebuild_source_credential.example will be created
  + resource "aws_codebuild_source_credential" "example" {
      + arn         = (known after apply)
      + auth_type   = "PERSONAL_ACCESS_TOKEN"
      + id          = (known after apply)
      + server_type = "GITHUB"
      + token       = (sensitive value)
    }

  # aws_ecr_repository.ecr_repository will be created
  + resource "aws_ecr_repository" "ecr_repository" {
      + arn                  = (known after apply)
      + id                   = (known after apply)
      + image_tag_mutability = "MUTABLE"
      + name                 = "bazel-build-container"
      + registry_id          = (known after apply)
      + repository_url       = (known after apply)
      + tags_all             = (known after apply)

      + image_scanning_configuration {
          + scan_on_push = true
        }
    }

  # aws_iam_role.codebuild_role will be created
  + resource "aws_iam_role" "codebuild_role" {
      + arn                   = (known after apply)
      + assume_role_policy    = jsonencode(
            {
              + Statement = [
                  + {
                      + Action    = "sts:AssumeRole"
                      + Effect    = "Allow"
                      + Principal = {
                          + Service = "codebuild.amazonaws.com"
                        }
                    },
                ]
              + Version   = "2012-10-17"
            }
        )
      + create_date           = (known after apply)
      + force_detach_policies = false
      + id                    = (known after apply)
      + managed_policy_arns   = (known after apply)
      + max_session_duration  = 3600
      + name                  = "codebuild_role"
      + name_prefix           = (known after apply)
      + path                  = "/"
      + tags_all              = (known after apply)
      + unique_id             = (known after apply)

      + inline_policy {
          + name   = (known after apply)
          + policy = (known after apply)
        }
    }

  # aws_iam_role_policy.codebuild_policy will be created
  + resource "aws_iam_role_policy" "codebuild_policy" {
      + id     = (known after apply)
      + name   = (known after apply)
      + policy = (known after apply)
      + role   = "codebuild_role"
    }

  # aws_s3_bucket.artifacts_output will be created
  + resource "aws_s3_bucket" "artifacts_output" {
      + acceleration_status         = (known after apply)
      + acl                         = "private"
      + arn                         = (known after apply)
      + bucket                      = "adroll-test-sandbox-2"
      + bucket_domain_name          = (known after apply)
      + bucket_regional_domain_name = (known after apply)
      + force_destroy               = false
      + hosted_zone_id              = (known after apply)
      + id                          = (known after apply)
      + object_lock_enabled         = (known after apply)
      + region                      = (known after apply)
      + request_payer               = (known after apply)
      + tags_all                    = (known after apply)
      + website_domain              = (known after apply)
      + website_endpoint            = (known after apply)

      + object_lock_configuration {
          + object_lock_enabled = (known after apply)

          + rule {
              + default_retention {
                  + days  = (known after apply)
                  + mode  = (known after apply)
                  + years = (known after apply)
                }
            }
        }

      + versioning {
          + enabled    = (known after apply)
          + mfa_delete = (known after apply)
        }
    }

  # aws_s3_bucket_acl.artifacts_output will be created
  + resource "aws_s3_bucket_acl" "artifacts_output" {
      + acl    = "private"
      + bucket = (known after apply)
      + id     = (known after apply)

      + access_control_policy {
          + grant {
              + permission = (known after apply)

              + grantee {
                  + display_name  = (known after apply)
                  + email_address = (known after apply)
                  + id            = (known after apply)
                  + type          = (known after apply)
                  + uri           = (known after apply)
                }
            }

          + owner {
              + display_name = (known after apply)
              + id           = (known after apply)
            }
        }
    }

  # aws_s3_bucket_ownership_controls.artifacts_output_ownership_controls will be created
  + resource "aws_s3_bucket_ownership_controls" "artifacts_output_ownership_controls" {
      + bucket = (known after apply)
      + id     = (known after apply)

      + rule {
          + object_ownership = "BucketOwnerPreferred"
        }
    }

Plan: 9 to add, 0 to change, 0 to destroy.
```

```
terraform apply
(need someone with sre permissions to run this)

aws codebuild start-build --project-name bazel-build-container --region us-west-2
(need someone with sre permissions to run this)
```

Check status of build at https://us-west-2.console.aws.amazon.com/codesuite/codebuild/projects

When the bazel-build-container finishes building, build the artifacts:

```
aws codebuild start-build --project-name aggregation-service-artifacts-build --region us-west-2
(need someone with sre permissions to run this)
```


Running into this error in the CodeBuild log:

```
Build 'amazon-ebs.sample-ami' errored after 936 milliseconds 511 microseconds: VPCIdNotSpecified: No default VPC for this user
    status code: 400, request id: fffa8013-121f-4855-a665-70e36030a4e7x
```

Create a default VPC:

```
aws ec2 create-default-vpc --region us-west-2
```

The artifacts should build successfully after this.
