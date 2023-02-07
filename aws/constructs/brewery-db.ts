import {Construct} from "constructs";
import * as rds from 'aws-cdk-lib/aws-rds';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import {SecretValue} from "aws-cdk-lib";
import {BreweryVpc} from "./brewery-vpc";
import {DatabaseInstance} from "aws-cdk-lib/aws-rds";

export interface DbProps {
    user: string,
    password: string,
    vpc: BreweryVpc,
}

/**
 * Construct definition for a single, (non-clustered) RDS instance, running MySQL on EC2 small instance
 */
export class BreweryMySql extends Construct {

    public readonly db: DatabaseInstance;

    constructor(scope: Construct, id: string, props: DbProps) {
        super(scope, id);

        //todo dont use for production
        const secret = SecretValue.unsafePlainText(props.password);

        const db = new rds.DatabaseInstance(this, 'brewery-mysql', {
            engine: rds.DatabaseInstanceEngine.mysql({ version: rds.MysqlEngineVersion.VER_8_0_30 }),
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.BURSTABLE3, ec2.InstanceSize.SMALL),
            credentials: {
                username: props.user,
                password: secret
            },
            vpc: props.vpc.vpc,
            allocatedStorage: 20,
            multiAz: false,
            securityGroups: [props.vpc.mysql_sg],
        });
        this.db = db;
    }
}

