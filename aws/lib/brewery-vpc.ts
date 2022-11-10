import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {SubnetType} from 'aws-cdk-lib/aws-ec2';
import {Construct} from "constructs";
import {EurekaService} from "../constructs/eureka-service";
import {ConfigService} from "../constructs/config-service";
import {ZipkinService} from "../constructs/zipkin-service";
import {ArtemisService} from "../constructs/artemis-service";

export class BreweryVpc extends Construct {
    // to get current region: Stack.of(this).region

    public readonly PUBLIC_SUBNET_NAME = 'brewery-public';
    public readonly PRIVATE_SUBNET_NAME = 'brewery-private'
    public readonly vpc: ec2.Vpc;


    public readonly mysql_sg: ec2.SecurityGroup;
    public readonly eureka_sg: ec2.SecurityGroup;
    public readonly config_sg: ec2.SecurityGroup;
    public readonly artemis_jms_sg: ec2.SecurityGroup;
    public readonly zipkin_sg: ec2.SecurityGroup;
    public readonly ssh_sg: ec2.SecurityGroup;
    public readonly microservice_sg: ec2.SecurityGroup;
    public readonly http_sg: ec2.SecurityGroup;

    constructor(scope: Construct, id: string) {
        super(scope, id);

        // the default VPC configuration creates public and private subnets
        // spanning the size of the VPC; however, we want a private subnet (with NAT Gateway) and a PUBLIC subnet
        // Instances launched into this vpc will use 'default' instance tenancy
        const vpc = new ec2.Vpc(this, 'brewery-vpc', {
            vpcName: "brewery-microservices",
            maxAzs: 2,
            natGateways: 1,
            cidr: '10.0.0.0/16',
            subnetConfiguration: [
                {
                    subnetType: ec2.SubnetType.PUBLIC,
                    name: this.PUBLIC_SUBNET_NAME,
                    cidrMask: 24
                },
                {
                    subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
                    name: this.PRIVATE_SUBNET_NAME,
                }
            ],
        });

        this.vpc = vpc;
        this.artemis_jms_sg = this.buildArtemisJmsSecurityGroup();
        this.zipkin_sg = this.buildZipkinSecurityGroup();
        this.ssh_sg = this.buildSshSecurityGroup();
        this.microservice_sg = this.buildMicroserviceSecurityGroup();
        this.mysql_sg = this.buildMySqlSecurityGroup();
        this.http_sg = this.buildHttpSecurityGroup();
        this.eureka_sg = this.buildEurekaSecurityGroup();
        this.config_sg = this.buildConfigSecurityGroup();
    }

    private buildMySqlSecurityGroup(): ec2.SecurityGroup {
        const mysql = new ec2.SecurityGroup(this, 'MySqlSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for MySql database servers',
            allowAllOutbound: true
        });
        mysql.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(3306), 'allow port 3306 access from the world');
        return mysql;
    }

    private buildArtemisJmsSecurityGroup(): ec2.SecurityGroup {
        const jms = new ec2.SecurityGroup(this, 'JmsSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for Artemis JMS that allows 61616, 8161',
            allowAllOutbound: true,
        });
        jms.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(61616), 'allow port 61616 access from the world');
        jms.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(ArtemisService.WEB_CONSOLE_PORT), 'allow port 8161 access from the world');
        return jms;
    }

    private buildZipkinSecurityGroup(): ec2.SecurityGroup {
        const sg = new ec2.SecurityGroup(this, 'ZipkinSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for Zipkin server that allows port 9411',
            allowAllOutbound: true,
        });
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(ZipkinService.PORT), 'allow port 9411 access from the world');
        return sg;
    }

    private buildEurekaSecurityGroup(): ec2.SecurityGroup {
        const sg = new ec2.SecurityGroup(this, 'EurekaSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for Eureka server that allows port 8761',
            allowAllOutbound: true,
        });
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(EurekaService.PORT), 'allow port 9411 access from the world');
        return sg;
    }

    private buildConfigSecurityGroup(): ec2.SecurityGroup {
        const sg = new ec2.SecurityGroup(this, 'ConfigSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for Configuration server that allows port 8888',
            allowAllOutbound: true,
        });
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(ConfigService.PORT), 'allow port 9411 access from the world');
        return sg;
    }

    private buildSshSecurityGroup(): ec2.SecurityGroup {
        const sg = new ec2.SecurityGroup(this, 'VpcEndpointSecurityGroup', {
            vpc: this.vpc,
            description: 'SG that allows SSH',
            allowAllOutbound: true,
        });
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(22), 'allow port 22 access from the world');
        return sg;
    }

    /**
     * allows access to the ports used by every brewery service, from the world
     */
    private buildMicroserviceSecurityGroup(): ec2.SecurityGroup {
        const sg = new ec2.SecurityGroup(this, 'MicroserviceSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for all microservices running in a cluster',
            allowAllOutbound: true,
        });
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8080), 'allow port range 8080 from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8081), 'allow port range 8081 from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8082), 'allow port range 8082 from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8083), 'allow port range 8083 from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(61616), 'allow port range 61616, artemis JMS, from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8761), 'allow port range 8761, for eureka, from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8888), 'allow port range 8888, for config server, from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(8161), 'allow port range 8161, artemis web server, from the world');
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(9411), 'allow port range 9411, zipkin, from the world');
        return sg;
    }

    private buildHttpSecurityGroup(): ec2.SecurityGroup {
        const sg = new ec2.SecurityGroup(this, 'HttpSecurityGroup', {
            vpc: this.vpc,
            description: 'SG for services that need HTTP access on port 80',
            allowAllOutbound: true,
        });
        sg.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(80), 'allow port 80 from the world');
        return sg;
    }



}