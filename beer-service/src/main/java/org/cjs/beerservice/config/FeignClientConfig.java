package org.cjs.beerservice.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sets up in interceptor for the feign client to add HTTP Basic Authentication to it
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(@Value("${org.cjs.inventory-user}") String inventoryUser,
                                                                   @Value("${org.cjs.inventory-password}")String inventoryPassword) {

        return new BasicAuthRequestInterceptor(inventoryUser, inventoryPassword);
    }
}
