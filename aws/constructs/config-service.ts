import {Construct} from "constructs";
import * as ecs from "aws-cdk-lib/aws-ecs";
import {NetworkMode} from "aws-cdk-lib/aws-ecs";
import {BreweryServiceProps} from "../lib/brewery-service-props";
import {
    buildService,
    CONTAINER_MEMORY_RESERVATION_MIB,
    eurekaClientServiceUrlDefaultZone,
    SPRING_PROFILE_ACTIVE
} from "./brewery-service";

/**
 * Construct that defines an ECS task definition and ECS service for spring cloud configuration server
 */
export class ConfigService extends Construct {

    public static readonly IMAGE_NAME = 'strohs/brewery-config-server';

    // config server's web console port
    public static readonly PORT = 8888;

    taskDefinition: ecs.TaskDefinition;
    service: ecs.Ec2Service;

    constructor(scope: Construct, id: string, props: BreweryServiceProps) {
        super(scope, id);

        this.taskDefinition = this.buildTaskDef(props);
        this.service = buildService(this, this.taskDefinition, props);
    }

    protected buildTaskDef(props: BreweryServiceProps): ecs.Ec2TaskDefinition {
        const taskDefinition = new ecs.Ec2TaskDefinition(this, `${props.serviceName}Task`, {
            networkMode: NetworkMode.AWS_VPC
        });

        taskDefinition.addContainer(`${props.serviceName}Container`, {
            image: ecs.ContainerImage.fromRegistry(ConfigService.IMAGE_NAME),
            //hostname: 'the name of one container can be entered in the links of another container. This is to connect the containers.',
            containerName: props.serviceName,
            portMappings: [
                {
                    containerPort: ConfigService.PORT,
                    hostPort: ConfigService.PORT,
                    protocol: ecs.Protocol.TCP,
                }
            ],
            environment: {
                EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: eurekaClientServiceUrlDefaultZone,
                EUREKA_INSTANCE_HOSTNAME: `${props.serviceName}.${props.namespace.namespaceName}`,
                SPRING_PROFILES_ACTIVE: SPRING_PROFILE_ACTIVE,
            },
            memoryReservationMiB: CONTAINER_MEMORY_RESERVATION_MIB,
            //cpu: this.CONTAINER_CPU_UNITS,
            logging: ecs.LogDrivers.awsLogs({ streamPrefix: props.serviceName })
        });
        return taskDefinition;
    }

}