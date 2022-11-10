# Welcome to your CDK TypeScript project

This is a blank project for CDK development with TypeScript.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## Useful commands

* `npm run build`   compile typescript to js
* `npm run watch`   watch for changes and compile
* `npm run test`    perform the jest unit tests
* `cdk deploy`      deploy this stack to your default AWS account/region
* `cdk diff`        compare deployed stack with current state
* `cdk synth`       emits the synthesized CloudFormation template



## connecting to an RDS MySQL instance on a private subnet from a public bastion server
copy your sql ddl file(s) to the remote bastion server
> scp -i ~/cliff-aws-keypair.pem mysql-init-all.sql ec2-user@ec2-3-95-147-224.compute-1.amazonaws.com:./

ssh to bastion server
> ssh -i PATH_TO_.PEM ec2-user@BASTION_SERVER_DNS_NAME_OR_IP

install mariadb (used for connecting to mysql)
> sudo yun install mariadb
> mysql --version

connect to the remote mysql server
> mysql -h DB_PRIVATE_HOSTNAME_OR_IP -P 3306 -u MASTER_USER_NAME -p

enter password when prompted

now run your ddl
> \. LOCAL_DDL_FILENAME

check if databases created
> SHOW DATABASES;

check if users created
> SELECT User, Host FROM mysql.user;



## ECS Best practices
might want to run the Zipkin task on fargate, in the public subnet

- give each ecs service its own security group
- recommended to use `awsvpc` network mode
  - allows each task to have a unique IP address with a service level security group
  - containers that belong to the same task can communicate over the `localhost` interface
  - **the `awsvpc` network mode doesn't provide task ENIs with public IP addresses**; therefore they can not make direct use of an internet gateway
    - To access the internet, tasks must be launched in a private subnet that's configured to use a NAT gateway
    - Tasks that are launched within public subnets do not have access to the internet.
  - Services with tasks that use the `awsvpc` network mode only support Application Load Balancer and Network Load Balancer
    - When you create any target groups for these services, you must choose `ip` as the target type. Do not use `instance`

- Amazon ECS populates the hostname of a `task` with an Amazon-provided (internal) DNS hostname when both the `enableDnsHostnames` and `enableDnsSupport` options are enabled on your VPC



## Getting Public IP addresses of EC2 instances using instance id
1. `aws ec2 describe-instances --filters "Name=instance-state-name,Values=running"` can be used to get instance IDs of RUNNING instances

2. `aws ec2 describe-instances --instance-ids i-0bf71a557f74ee485 --query 'Reservations[*].Instances[*].PublicIpAddress' --output text`

OR

```
aws --region us-east-1 \
ec2 describe-instances \
--filters \
"Name=instance-state-name,Values=running" \
"Name=instance-id,Values=i-0bf71a557f74ee485" \
--query 'Reservations[*].Instances[*].[PrivateIpAddress, PublicIpAddress]' \
--output text
```

### describe a specific cluster
```
aws ecs describe-clusters --include ATTACHMENTS --clusters  BreweryServicesStack-brewerycluster0B1958CE-dBg3A4HzF7pA
```

### describe one or more task(s) in a cluster (using task ARN or task ID)
```
aws ecs describe-tasks --cluster MyCluster --tasks "74de0355a10a4f979ac495c14EXAMPLE" "d789e94343414c25b9f6bd59eEXAMPLE"
```

### describe the tasks running in a service
> aws ecs describe-services --services zipkin-service


### listing cloud map namespaces
> aws servicediscovery list-namespaces