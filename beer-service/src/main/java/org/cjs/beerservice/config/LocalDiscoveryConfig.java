package org.cjs.beerservice.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Config that enables this application as a Eureka Client
 */
@Profile("local-discovery")
@Configuration
@EnableDiscoveryClient
public class LocalDiscoveryConfig {
}
