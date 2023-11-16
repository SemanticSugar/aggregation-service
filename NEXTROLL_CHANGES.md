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

Go to AWS Console > AMIs and search for the AMI named aggregation-service-enclave... Add the following tags:

```
pillar: insights_attribution
team: data_pipelines
```

Otherwise, the AMI could get [deregistered](https://adroll.atlassian.net/wiki/spaces/EN/pages/105709614/Tagging+Policies#Automated-scripts-that-delete-resources-that-are-against-our-policies) for not complying with our tagging scheme.

Download the artifacts.

```
cd ~/projects/aggregation-service/terraform/aws
mkdir -p jars
aws s3 cp s3://adroll-aggregation-service-build-artifacts/aggregation-service/$(cat ../../VERSION)/ jars/ --recursive
bash fetch_terraform.sh
```

## Set up your deployment environment

This section goes through the steps of [setting up your deployment environmen](https://github.com/privacysandbox/aggregation-service/blob/b5c9c0015220b4631e6a8ca1df9ea36e20c32dee/docs/aws-aggregation-service.md#set-up-your-deployment-environment).

```
cd ~/projects/aggregation-service/terraform/aws/environments
mkdir dev
cp -R demo/* dev
cd dev
```

```
vi main.tf
# Configure it as follows:
  backend "s3" {
    bucket         = "adroll-tfstate"
    dynamodb_table = "TerraformLock"
    region         = "us-west-2"
    key            = "udp/aggregation-service/environments/dev/terraform.tfstate"
  }
```

Rename example.auto.tfvars to dev.auto.tfvars and add the ...assume_role... values using the information you received in the onboarding email.

```
mv example.auto.tfvars dev.auto.tfvars
vi dev.auto.tfvars
# Configure it as follows:
region      = "us-west-2"
environment = "aggregation-serice-dev-env"
. . . . . . . . . .
coordinator_a_assume_role_parameter = "arn:aws:iam::850159350730:role/a_771945457201_coordinator_assume_role"
coordinator_b_assume_role_parameter = "arn:aws:iam::311771262672:role/b_771945457201_coordinator_assume_role"
. . . . . . . . . .
alarm_notification_email = "data-pipelines@nextroll.com"
```

Copy the contents of the release_params.auto.tfvars file into a new file self_build_params.auto.tfvars remove the release_params.auto.tfvars file afterwards.

```
cp -L release_params.auto.tfvars self_build_params.auto.tfvars
rm release_params.auto.tfvars
```

And change the line ami_owners = ["971056657085"] to ami_owners = ["self"] in your self_build_params.auto.tfvars

```
ami_owners = ["self"]
```


Ensure proper tags are specified in EC2 instances. Otherwise, the EC2 instances could get [killed](https://adroll.atlassian.net/wiki/spaces/EN/pages/105709614/Tagging+Policies#Automated-scripts-that-delete-resources-that-are-against-our-policies) for not complying with our tagging scheme.

```
cd ~/projects/aggregation-service/terraform/aws/coordinator-services-and-shared-libraries/operator/terraform/aws/modules/worker
vi main.tf
# Add these tags to worker_template:
      application        = "aggregation-service"
      env                = "production"
      pillar             = "insights_attribution"
      team               = "data_pipelines"
```

If you are updating resources instead of creating them from scratch, add the nextroll-instance
to the managed_policy_arns. This must have gotten added automatically along the way. If you don't add this, terraform will attempt to remove it and you will get this error: "unable to detach policies: AccessDenied: User: arn:aws:sts::771945457201:assumed-role/sre/jonathan.aquino is not authorized to perform: iam:DetachRolePolicy on resource: role aggregation-service-dev-env-AggregationServiceWorkerRole with an explicit deny in an identity-based policy"

```
cd ~/projects/aggregation-service/terraform/aws/coordinator-services-and-shared-libraries/operator/terraform/aws/modules/worker
vi main.tf
# Update managed_policy_arns in enclave_role:
  managed_policy_arns = [
    data.aws_iam_policy.SSMManagedInstanceCore.arn,
    "arn:aws:iam::771945457201:policy/nextroll-instance"
  ]
```

Terraform commands:

```
cd ~/projects/aggregation-service/terraform/aws/environments/dev
terraform init
terraform plan
terraform apply
```

See [terraform plan output](terraform_plan.txt).

Success!

```
Apply complete! Resources: 191 added, 0 changed, 0 destroyed.

Outputs:

create_job_endpoint = "POST https://zsiw2k3pcb.execute-api.us-west-2.amazonaws.com/stage/v1alpha/createJob"
frontend_api_id = "zsiw2k3pcb"
get_job_endpoint = "GET https://zsiw2k3pcb.execute-api.us-west-2.amazonaws.com/stage/v1alpha/getJob"
```

To test the APIs:

```
# Get the AccessKeyId, SecretAccessKey, and Token.
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/hologram-access

curl https://zsiw2k3pcb.execute-api.us-west-2.amazonaws.com/stage/v1alpha/getJob?job_request_id=16092ed8-9504-4dc1-bf17-26919cc95489 --user "$ACCESS_KEY":"$SECRET_KEY" --aws-sigv4 "aws:amz:us-west-2:execute-api" -H "x-amz-security-token: $TOKEN"

curl -X POST -d @post.txt https://zsiw2k3pcb.execute-api.us-west-2.amazonaws.com/stage/v1alpha/createJob --user "$ACCESS_KEY":"$SECRET_KEY" --aws-sigv4 "aws:amz:us-west-2:execute-api" -H "x-amz-security-token: $TOKEN" --header "Content-Type:application/json"

where post.txt contains

{
    "input_data_blob_prefix": "attribution-reporting/report-aggregate-attribution/date=2023-11-14/requests/16092ed8-9504-4dc1-bf17-26919cc95489/input/reports.avro",
    "input_data_bucket_name": "adroll-aggregation-service-data",
    "output_data_blob_prefix": "attribution-reporting/report-aggregate-attribution/date=2023-11-14/requests/16092ed8-9504-4dc1-bf17-26919cc95489/output/output.avro",
    "output_data_bucket_name": "adroll-aggregation-service-data",
    "job_parameters": {
        "attribution_report_to": "d.adroll.com",
        "output_domain_blob_prefix": "attribution-reporting/report-aggregate-attribution/date=2023-11-14/domain/domain.avro",
        "output_domain_bucket_name": "adroll-aggregation-service-data"
    },
    "job_request_id": "16092ed8-9504-4dc1-bf17-26919cc95489"
}
```
