import * as cdk from 'aws-cdk-lib';
import {MultiStackProps} from "./multi-stack-props";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import {InstanceClass, InstanceSize} from "aws-cdk-lib/aws-ec2";
import {ApplicationProtocol} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import {EurekaService} from "../constructs/eureka-service";
import {ZipkinService} from "../constructs/zipkin-service";
import {ArtemisService} from "../constructs/artemis-service";
import {ConfigService} from "../constructs/config-service";
import {InventoryService} from "../constructs/inventory-service";
import {InventoryFailoverService} from "../constructs/inventory-failover-service";
import {BeerService} from "../constructs/beer-service";
import {OrderService} from "../constructs/order-service";
import {buildClusterWithAsg} from "../constructs/brewery-service";

export class BreweryServicesStack extends cdk.Stack {

    public readonly cluster: ecs.ICluster;
    public readonly zipkinService: ZipkinService;
    public readonly artemisService: ArtemisService;
    public readonly eurekaService: EurekaService;
    public readonly configService: ConfigService;
    public readonly inventoryService: InventoryService;
    public readonly inventoryFailoverService: InventoryFailoverService;
    public readonly beerService: BeerService;
    public readonly orderService: OrderService;

    constructor(scope: cdk.App, id: string, props: MultiStackProps) {
        super(scope, id, props);

        /// Build a cluster for our single Zipkin server
        const zipkinCluster = buildClusterWithAsg(this, {
            id: 'Zipkin',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.zipkin_sg]
        });

        // build a zipkin service and place it in the cluster
        const zipkinService = new ZipkinService(this, 'ZipkinService', {
            cluster: zipkinCluster,
            vpc: props.vpc.vpc,
            serviceName: 'zipkin',
            namespace: props.namespace,
            securityGroups: [props.vpc.zipkin_sg],
        });

        // allow access to the zipkin console from our application load balancer
        props.alb
            .addListener('ZipkinListener', {
                open: true,
                port: ZipkinService.PORT,
                protocol: ApplicationProtocol.HTTP,
            })
            .addTargets('ZipkinServiceTarget', {
                port: ZipkinService.PORT,
                targets: [zipkinService.service],
                protocol: ApplicationProtocol.HTTP,
                healthCheck: {
                    path: '/health',
                    port: String(ZipkinService.PORT),
                }
            });
        this.zipkinService = zipkinService;



        /// Build a cluster for Artemis JMS, ideally, we could swap Artemis for AWS SQS, or Amazon MQ
        const artemisCluster = buildClusterWithAsg(this, {
            id: 'Artemis',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.artemis_jms_sg]
        });

        /// Build an artemis JMS service
        this.artemisService = new ArtemisService(this, 'ArtemisService', {
            cluster: artemisCluster,
            vpc: props.vpc.vpc,
            serviceName: 'artemis',
            namespace: props.namespace,
            securityGroups: [props.vpc.artemis_jms_sg],
        });


        /// Build a eureka cluster
        const eurekaCluster = buildClusterWithAsg(this, {
            id: 'Eureka',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.eureka_sg]
        });

        const eurekaService = new EurekaService(this, 'EurekaService', {
            cluster: eurekaCluster,
            vpc: props.vpc.vpc,
            serviceName: 'eureka',
            namespace: props.namespace,
            securityGroups: [props.vpc.eureka_sg]
        });

        // allow access to the eureka web console (netflix:eureka) from our application load balancer
        props.alb
            .addListener('EurekaListener', {
                open: true,
                port: EurekaService.PORT,
                protocol: ApplicationProtocol.HTTP
            })
            .addTargets('EurekaServiceTargets', {
                port: EurekaService.PORT,
                targets: [eurekaService.service],
                protocol: ApplicationProtocol.HTTP,
                healthCheck: {
                    path: '/actuator/health',
                    port: String(EurekaService.PORT),
                }
            });
        this.eurekaService = eurekaService;


        //// Build a Configuration server cluster
        const configCluster = buildClusterWithAsg(this, {
            id: 'Config',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.config_sg]
        });

        const configService = new ConfigService(this, 'ConfigService', {
            cluster: configCluster,
            vpc: props.vpc.vpc,
            serviceName: 'config',
            namespace: props.namespace,
            securityGroups: [props.vpc.config_sg]
        });

        // allow access to the configuration servers web console (MyUserName:MySecretPassword) from our application load balancer
        props.alb
            .addListener('ConfigListener', {
                open: true,
                port: ConfigService.PORT,
                protocol: ApplicationProtocol.HTTP
            })
            .addTargets('ConfigServiceTargets', {
                port: ConfigService.PORT,
                targets: [configService.service],
                protocol: ApplicationProtocol.HTTP,
                healthCheck: {
                    path: '/actuator/health',
                    port: String(ConfigService.PORT),
                }
            });
        this.configService = configService;




        //// Build the inventory-service
        const inventoryCluster = buildClusterWithAsg(this, {
            id: 'InventoryService',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.config_sg]
        });
        this.inventoryService = new InventoryService(this, 'InventoryService', {
            cluster: inventoryCluster,
            vpc: props.vpc.vpc,
            serviceName: 'inventory-service',
            namespace: props.namespace,
            securityGroups: [props.vpc.microservice_sg],
        });

        //// Build the inventory-failover-service
        const inventoryFailoverCluster = buildClusterWithAsg(this, {
            id: 'InventoryFailoverService',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.config_sg]
        });
        this.inventoryFailoverService = new InventoryFailoverService(this, 'InventoryFailoverService', {
            cluster: inventoryFailoverCluster,
            vpc: props.vpc.vpc,
            serviceName: 'inventory-failover-service',
            namespace: props.namespace,
            securityGroups: [props.vpc.microservice_sg],
        });

        //// Build the beer-service
        const beerServiceCluster = buildClusterWithAsg(this, {
            id: 'BeerService',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.config_sg]
        });
        this.beerService = new BeerService(this, 'BeerService', {
            cluster: beerServiceCluster,
            vpc: props.vpc.vpc,
            serviceName: 'beer-service',
            namespace: props.namespace,
            securityGroups: [props.vpc.microservice_sg],
        });

        //// Build the order service
        const orderServiceCluster = buildClusterWithAsg(this, {
            id: 'OrderService',
            vpc: props.vpc.vpc,
            instanceType: ec2.InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM),
            minCapacity: 1,
            maxCapacity: 1,
            securityGroups: [props.vpc.config_sg]
        });
        this.orderService = new OrderService(this, 'OrderService', {
            cluster: orderServiceCluster,
            vpc: props.vpc.vpc,
            serviceName: 'order-service',
            namespace: props.namespace,
            securityGroups: [props.vpc.microservice_sg],
        });
    }

    // creates an entry into this stack's 'output' section within cloudformation
    private createOutput(scope: cdk.Stack, id: string, value: string, description: string): cdk.CfnOutput {
        return new cdk.CfnOutput(scope, id, {
            value: value,
            description: description,
        })
    }
}