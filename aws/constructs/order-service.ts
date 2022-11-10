import {Construct} from "constructs";
import * as ecs from "aws-cdk-lib/aws-ecs";
import {BreweryServiceProps} from "../lib/brewery-service-props";
import {NetworkMode} from "aws-cdk-lib/aws-ecs";
import {
    buildService,
    configServerUri, CONTAINER_MEMORY_RESERVATION_MIB,
    eurekaClientServiceUrlDefaultZone,
    SPRING_PROFILE_ACTIVE,
    DEFAULT_NAMESPACE
} from "./brewery-service";
import {BeerService} from "./beer-service";


export class OrderService extends Construct {

    public static readonly IMAGE_NAME = `strohs/beer-order-service`;
    public static readonly PORT = 8081;

    taskDefinition: ecs.TaskDefinition;
    service: ecs.Ec2Service;

    constructor(scope: Construct, id: string, props: BreweryServiceProps) {
        super(scope, id);

        this.taskDefinition = this.buildTaskDef(props);
        this.service = buildService(this, this.taskDefinition, props);
    }

    protected buildTaskDef(props: BreweryServiceProps): ecs.Ec2TaskDefinition {
        const taskDefinition = new ecs.Ec2TaskDefinition(this, `${props.serviceName}Task`, {
            networkMode: NetworkMode.AWS_VPC,
        });

        // add the eureka container to the task definition
        taskDefinition.addContainer(`${props.serviceName}Container`, {
            image: ecs.ContainerImage.fromRegistry(OrderService.IMAGE_NAME),
            //hostname: 'the name of one container can be entered in the links of another container. This is to connect the containers.',
            containerName: props.serviceName,
            portMappings: [
                {
                    containerPort: OrderService.PORT,
                    hostPort: OrderService.PORT,
                    protocol: ecs.Protocol.TCP,
                }
            ],
            environment: {
                EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: eurekaClientServiceUrlDefaultZone,
                EUREKA_INSTANCE_HOSTNAME: `${props.serviceName}.${props.namespace.namespaceName}`,
                SPRING_PROFILES_ACTIVE: SPRING_PROFILE_ACTIVE,
                SPRING_CLOUD_CONFIG_URI: configServerUri,
                ORG_CJS_BEER_SERVICE_HOST: `http://beer-service.${DEFAULT_NAMESPACE}:${BeerService.PORT}`,
            },
            memoryReservationMiB: CONTAINER_MEMORY_RESERVATION_MIB,
            //cpu: this.CONTAINER_CPU_UNITS,
            logging: ecs.LogDrivers.awsLogs({ streamPrefix: props.serviceName })
        });
        return taskDefinition;
    }
}