import {Construct} from "constructs";
import * as ecs from "aws-cdk-lib/aws-ecs";
import {BreweryServiceProps} from "../lib/brewery-service-props";
import {NetworkMode} from "aws-cdk-lib/aws-ecs";
import {ZipkinService} from "./zipkin-service";
import {buildService, CONTAINER_MEMORY_RESERVATION_MIB} from "./brewery-service";

/**
 * Construct that defines an ECS taskDefinition and ECS service for a eureka service discovery service
 * NOTE that, ideally you could skip Eureka alltogether and simply use AWS CloudMap for service discovery. However,
 * this would require a major rewrite of our existing microservices.
 */
export class EurekaService extends Construct {

    public static readonly IMAGE_NAME = 'strohs/brewery-eureka';
    public static readonly PORT = 8761;

    taskDefinition: ecs.TaskDefinition;
    service: ecs.Ec2Service;

    constructor(scope: Construct, id: string, props: BreweryServiceProps) {
        super(scope, id);

        this.taskDefinition = this.buildTaskDef(props);
        this.service = buildService(this, this.taskDefinition, props);

    }

    protected buildTaskDef(props: BreweryServiceProps): ecs.Ec2TaskDefinition {
        // create a eureka task definition
        const taskDefinition = new ecs.Ec2TaskDefinition(this, `${props.serviceName}Task`, {
            networkMode: NetworkMode.AWS_VPC,
        });

        // add the eureka container to the task definition
        taskDefinition.addContainer(`${props.serviceName}Container`, {
            image: ecs.ContainerImage.fromRegistry(EurekaService.IMAGE_NAME),
            //hostname: 'the name of one container can be entered in the links of another container. This is to connect the containers.',
            containerName: props.serviceName,
            portMappings: [
                {
                    containerPort: EurekaService.PORT,
                    hostPort: EurekaService.PORT,
                    protocol: ecs.Protocol.TCP,
                }
            ],
            environment: {
                SPRING_ZIPKIN_BASEURL: ZipkinService.ZIPKIN_BASE_URL,
            },
            memoryReservationMiB: CONTAINER_MEMORY_RESERVATION_MIB,
            //cpu: this.CONTAINER_CPU_UNITS,
            logging: ecs.LogDrivers.awsLogs({ streamPrefix: props.serviceName })
        });
        return taskDefinition;
    }

}