import {Construct} from "constructs";
import {BreweryServiceProps} from "../lib/brewery-service-props";
import {NetworkMode} from "aws-cdk-lib/aws-ecs";
import * as ecs from "aws-cdk-lib/aws-ecs";
import {buildService, CONTAINER_MEMORY_RESERVATION_MIB} from "./brewery-service";


export class ArtemisService extends Construct {

    public static readonly WEB_CONSOLE_PORT = 8161;
    public static readonly JMS_PORT = 61616;
    public static readonly IMAGE_NAME = 'strohs/artemis-temurin11:2.24.0';

    taskDefinition: ecs.TaskDefinition;
    service: ecs.Ec2Service;

    constructor(scope: Construct, id: string, props: BreweryServiceProps) {
        super(scope, id);

        const taskDefinition = this.buildTaskDef(props);
        const service = buildService(this, taskDefinition, props);

        this.taskDefinition = taskDefinition;
        this.service = service;
    }

    protected buildTaskDef(props: BreweryServiceProps): ecs.Ec2TaskDefinition {
        // create a task definition
        const taskDefinition = new ecs.Ec2TaskDefinition(this, `${props.serviceName}Task`, {
            networkMode: NetworkMode.AWS_VPC,
        });

        // add a container to the task definition
        taskDefinition.addContainer(`${props.serviceName}Container`, {
            image: ecs.ContainerImage.fromRegistry(ArtemisService.IMAGE_NAME),
            //hostname: 'the name of one container can be entered in the links of another container. This is to connect the containers.',
            containerName: props.serviceName,
            portMappings: [
                {
                    containerPort: ArtemisService.JMS_PORT,
                    hostPort: ArtemisService.JMS_PORT,
                    protocol: ecs.Protocol.TCP,
                },
                {
                    containerPort: ArtemisService.WEB_CONSOLE_PORT,
                    hostPort: ArtemisService.WEB_CONSOLE_PORT,
                    protocol: ecs.Protocol.TCP,
                }
            ],
            environment: {
                ARTEMIS_USER: 'artemis',
                ARTEMIS_PASSWORD: 'artemis',
                ANONYMOUS_LOGIN: 'false',
                EXTRA_ARGS: '--http-host 0.0.0.0 --relax-jolokia',
            },
            memoryReservationMiB:  CONTAINER_MEMORY_RESERVATION_MIB,
            //cpu: this.CONTAINER_CPU_UNITS,
            logging: ecs.LogDrivers.awsLogs({ streamPrefix: props.serviceName })
        });
        return taskDefinition;
    }

}