import * as cdk from 'aws-cdk-lib';
import {BreweryVpc} from "../constructs/brewery-vpc";
import {INamespace} from "aws-cdk-lib/aws-servicediscovery";
import * as elb from "aws-cdk-lib/aws-elasticloadbalancingv2";

/**
 * Holds common configuration properties that are shared between the stacks used by the brewery microservices
 *
 * The main properties being shared are:
 * - the vpc to place the services in
 * - the application load balancer that will be used to make some services available to the internet
 * - the CloudMap namespace name being used for service discovery
 */
export interface MultiStackProps extends cdk.StackProps {
    // the vpc where the stack resources will be created
    vpc: BreweryVpc,

    // the cloud map service discovery namespace to use for service discovery
    namespace: INamespace,

    // the internet facing, application load balancer used to route web console requests
    alb: elb.IApplicationLoadBalancer,

    // EC2 instance type to run brewery tasks instances on
    ec2InstanceType: string,

    clusterMinCapacity: number,

    clusterMaxCapacity: number,
}