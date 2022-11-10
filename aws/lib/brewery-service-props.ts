import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as servicediscovery from "aws-cdk-lib/aws-servicediscovery";

/**
 * Common properties that can be used to define ECS brewery services
 */
export interface BreweryServiceProps {
    // VPC the cluster is deployed to
    vpc: ec2.IVpc,

    // the cloud map service discovery namespace to use for service discovery
    namespace: servicediscovery.INamespace,

    // the cluster to deploy a service into
    cluster: ecs.ICluster,

    // any security groups to apply to the service
    securityGroups?: ec2.ISecurityGroup[],

    // the name of the service, which will be used to construct its full namespace name in AWS CloudMap
    serviceName: string,

}