import {Construct} from "constructs";
import * as ecs from "aws-cdk-lib/aws-ecs";
import {NetworkMode, Protocol} from "aws-cdk-lib/aws-ecs";
import {BreweryServiceProps} from "../lib/brewery-service-props";
import {buildService, CONTAINER_MEMORY_RESERVATION_MIB, DEFAULT_NAMESPACE} from "./brewery-service";

/**
 * Construct that defines the ECS taskDefinition and ECS service for the zipkin tracing service
 */
export class ZipkinService extends Construct {

    public static readonly IMAGE_NAME = 'openzipkin/zipkin';
    public static readonly PORT = 9411;
    public static readonly ZIPKIN_BASE_URL = `http://zipkin.${DEFAULT_NAMESPACE}`;

    taskDefinition: ecs.TaskDefinition;
    service: ecs.Ec2Service;

    constructor(scope: Construct, id: string, props: BreweryServiceProps) {
        super(scope, id);

        this.taskDefinition = this.buildTaskDef(props);
        this.service = buildService(this, this.taskDefinition, props);

    }

    protected buildTaskDef(props: BreweryServiceProps): ecs.Ec2TaskDefinition {

        const taskDefinition = new ecs.Ec2TaskDefinition(this, `${props.serviceName}_task`, {
            networkMode: NetworkMode.AWS_VPC,
        });

        taskDefinition.addContainer(`${props.serviceName}_container`, {
            image: ecs.ContainerImage.fromRegistry(ZipkinService.IMAGE_NAME),
            //hostname: 'the name of one container can be entered in the links of another container. This is to connect the containers.',
            containerName: props.serviceName,
            portMappings: [
                {
                    containerPort: ZipkinService.PORT,
                    hostPort: ZipkinService.PORT,
                    protocol: Protocol.TCP,
                }
            ],
            memoryReservationMiB: CONTAINER_MEMORY_RESERVATION_MIB,
            //cpu: this.CONTAINER_CPU_UNITS,
            logging: ecs.LogDrivers.awsLogs({ streamPrefix: props.serviceName }),
        });
        return taskDefinition;
    }

}