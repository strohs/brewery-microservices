#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { BreweryInfrastructureStack } from '../lib/brewery-infrastructure-stack';
import {BreweryServicesStack} from "../lib/brewery-services-stack";


const app = new cdk.App();

const infra_stack = new BreweryInfrastructureStack(app, 'BreweryInfrastructureStack', {
  /* If you don't specify 'env', this stack will be environment-agnostic.
   * Account/Region-dependent features and context lookups will not work,
   * but a single synthesized template can be deployed anywhere. */

  /* Uncomment the next line to specialize this stack for the AWS Account
   * and Region that are implied by the current CLI configuration. */
  // env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },

  /* Uncomment the next line if you know exactly what Account and Region you
   * want to deploy the stack to. */
  // env: { account: '123456789012', region: 'us-east-1' },

  /* For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html */
});

const servicesStack = new BreweryServicesStack(app, 'BreweryServicesStack', {
    vpc: infra_stack.vpc,
    namespace: infra_stack.namespace,
    alb: infra_stack.alb,
    ec2InstanceType: 't3.medium',
    clusterMinCapacity: 1,
    clusterMaxCapacity: 8,
});


// creates an entry into this stack's 'output' section within cloudformation
function createOutput(scope: cdk.Stack, id: string, value: string, description: string): cdk.CfnOutput {
    return new cdk.CfnOutput(scope, id, {
        value: value,
        description: description,
    })
}