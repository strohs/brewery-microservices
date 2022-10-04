package org.cjs.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * uses a eureka server to discover the locations of these services.
 * Eureka server is running on a separate instance.
 * Note that the uri properties are configured to use the "lb:" prefix which indicates that
 * load balancing should be used across the microservices.
 * The circuit breaker patter is used with the "inventory-service" route, we can use it because we have
 * Resilience4J on the classpath... the same config seen here would be used with Hystrix
 */
@Profile("local-discovery")
@Configuration
public class LoadBalancedRoutesConfig {

    @Bean
    public RouteLocator loadBalancedRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route("beer-service",
                        r -> r.path("/api/v1/beer*",
                                        "/api/v1/beer/*",
                                        "/api/v1/beerUpc/*")
                        .uri("lb://beer-service")
                )
                .route("order-service ",
                        r -> r.path("/api/v1/customers/**")
                        .uri("lb://order-service")
                )
                .route("inventory-service",
                        r -> r.path("/api/v1/beer/*/inventory")
                        .filters(f -> f.circuitBreaker(c -> c.setName("inventoryCB")
                                        .setFallbackUri("forward:/inventory-failover")
                                        .setRouteId("inv-failover")
                                    ))
                        .uri("lb://inventory-service")
                )
                .route("inventory-failover-service",
                        r -> r.path("/inventory-failover/**")
                        .uri("lb://inventory-failover")
                )
                .build();
    }

}
