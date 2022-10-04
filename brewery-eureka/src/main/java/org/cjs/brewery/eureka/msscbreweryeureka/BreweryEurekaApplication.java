package org.cjs.brewery.eureka.msscbreweryeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class BreweryEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BreweryEurekaApplication.class, args);
	}

}
