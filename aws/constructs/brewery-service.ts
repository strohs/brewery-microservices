import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import {Construct} from "constructs";
import * as autoscaling from "aws-cdk-lib/aws-autoscaling";
import {TaskDefinition} from "aws-cdk-lib/aws-ecs";
import {BreweryServiceProps} from "../lib/brewery-service-props";
import {DnsRecordType} from "aws-cdk-lib/aws-servicediscovery";

export const CONTAINER_MEMORY_RESERVATION_MIB = 512;
export const CONTAINER_CPU_UNITS = 1024;
export const SPRING_PROFILE_ACTIVE ='aws';
export const DEFAULT_NAMESPACE = 'brewery.com';

export const eurekaClientServiceUrlDefaultZone = `http://netflix:eureka@eureka.${DEFAULT_NAMESPACE}:8761/eureka`;
export const configServerUri = `http://config.${DEFAULT_NAMESPACE}:8888`;


export interface ClusterProps {
    // the 'base' name to apply to the id's of the cluster resources
    id: string,

    // the vpc to run the cluster in
    vpc: ec2.Vpc,

    // the EC2 instance tyoes to run in the cluster
    instanceType: ec2.InstanceType,

    // minimum number of instances to create in the cluster
    minCapacity: number,

    // maximum number of instances to create in the cluster
    maxCapacity: number,

    // the security group(s) to apply to the cluster
    securityGroups: ec2.SecurityGroup[],
}

/**
 * Build an ECS cluster, running on EC2 instances, that uses an AutoScalingGroup to scale the
 * cluster instances
 * @param construct
 * @param props
 */
export function buildClusterWithAsg(construct: Construct, props: ClusterProps): ecs.Cluster {
    const cluster = new ecs.Cluster(construct, `${props.id}Cluster`, {
        vpc: props.vpc
    });

    const asg = new autoscaling.AutoScalingGroup(construct, `${props.id}Asg`, {
        vpc: props.vpc,
        instanceType: props.instanceType,
        machineImage: ecs.EcsOptimizedImage.amazonLinux2(),
        minCapacity: props.minCapacity,
        maxCapacity: props.maxCapacity,
    });
    for (const sg of props.securityGroups) {
        asg.addSecurityGroup(sg);
    }

    const capProvider = new ecs.AsgCapacityProvider(construct, `${props.id}CapProvider`, {
        autoScalingGroup: asg,
    });
    cluster.addAsgCapacityProvider(capProvider);

    return cluster;
}

export function buildService(construct: Construct, taskDefinition: TaskDefinition, props: BreweryServiceProps): ecs.Ec2Service {
    return new ecs.Ec2Service(construct, `${props.serviceName}Service`, {
        cluster: props.cluster,
        serviceName: props.serviceName,
        taskDefinition: taskDefinition,
        cloudMapOptions: {
            name: props.serviceName,
            cloudMapNamespace: props.namespace,
            dnsRecordType: DnsRecordType.A,
            //container: taskDefinition.findContainer(this.SERVICE_NAME),
            //containerPort: ,
        },
        securityGroups: props.securityGroups,
    });
}