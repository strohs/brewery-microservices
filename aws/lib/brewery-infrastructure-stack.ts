import * as cdk from 'aws-cdk-lib';
import {BreweryVpc} from "../constructs/brewery-vpc";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as servicediscovery from "aws-cdk-lib/aws-servicediscovery";
import * as elb from "aws-cdk-lib/aws-elasticloadbalancingv2";
import {BreweryMySql} from "../constructs/brewery-db";
import {DEFAULT_NAMESPACE} from "../constructs/brewery-service";

/**
 * This stack builds the AWS network infrastructure needed by our microservices:
 * - the vpc
 * - the cloudMap namespace
 * - an internet facing, application load balancer
 * - a bastion server on the public VPC so we can configure MySQL
 * - a single MySQL instance running within RDS on the private VPC
 */
export class BreweryInfrastructureStack extends cdk.Stack {

  public readonly vpc: BreweryVpc;
  public readonly namespace: servicediscovery.INamespace;
  public readonly alb: elb.IApplicationLoadBalancer;

  constructor(scope: cdk.App, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // create a vpc and a bastion ec2 instance that can be used to connect to the DB
    const vpc = new BreweryVpc(this, "BreweryVpc");
    this.vpc = vpc;

    // create a CloudMap namespace for the brewery services in order to enable service discovery
    const namespace = new servicediscovery.PrivateDnsNamespace(this, 'brewery-namespace', {
      name: DEFAULT_NAMESPACE,
      vpc: vpc.vpc,
    });
    this.namespace = namespace;

    // create an internet facing, application load balancer, that will route requests to web consoles
    // used in some of our services. Each service will create an appropriate listener and target group
    // on the load balancer
    const lb = new elb.ApplicationLoadBalancer(this, 'brewery-load-balancer', {
      vpc: vpc.vpc,
      internetFacing: true,
    });
    this.alb = lb;

    // build a mysql database in RDS
    const brewery_db = new BreweryMySql(this, 'brewery-db', {
      user: 'root',
      password: 'password',
      vpc: vpc,
    });

    // add the DB's endpoint address to CloudMap
    const dbService = namespace.createService('brewery-db-service', {
      name: 'mysqldb',
      dnsRecordType: servicediscovery.DnsRecordType.CNAME,
      dnsTtl: cdk.Duration.seconds(60),
    });

    dbService.registerCnameInstance('brewery-db-service-cname', {
      instanceCname: brewery_db.db.instanceEndpoint.hostname
    });

    // create a bastion server that can be used to connect to and configure the DB
    // configure the DB to allow connections from the bastion server
    const bastion = this.createBastionInstance();
    brewery_db.db.connections.allowFrom(bastion, ec2.Port.tcp(3306));


    // output load balancer public DNS address
    this.createOutput(this, 'brewery-application-lb', lb.loadBalancerDnsName, 'application load balancer public DNS name');
    this.createOutput(this, 'brewery-db-hostname', brewery_db.db.instanceEndpoint.hostname, 'brewery db hostname');
    this.createOutput(this, 'brewery-db-port', String(brewery_db.db.instanceEndpoint.port), 'brewery db port');
    this.createOutput(this, 'bastionPublicDns', bastion.instancePublicDnsName, 'bastion server public endpoint');
  }

  // creates an entry into this stack's 'output' section within cloudformation
  private createOutput(scope: cdk.Stack, id: string, value: string, description: string): cdk.CfnOutput {
    return new cdk.CfnOutput(scope, id, {
      value: value,
      description: description,
    })
  }

  // creates a bastion ec2 instance, in a public subnet of the vpc.
  // the keypair must already exist in the default region
  private createBastionInstance(): ec2.Instance {

    const keyPairName = this.node.tryGetContext('keyPairName');

    return new ec2.Instance(this, 'bastion', {
      vpc: this.vpc.vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PUBLIC,
      },
      securityGroup: this.vpc.ssh_sg,
      instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.BURSTABLE2,
          ec2.InstanceSize.MICRO,
      ),
      machineImage: new ec2.AmazonLinuxImage({
        generation: ec2.AmazonLinuxGeneration.AMAZON_LINUX_2
      }),
      keyName: keyPairName,
    });
  }
}
