![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-service.yml/badge.svg)
![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-inventory-service.yml/badge.svg)
![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-inventory-failover-service.yml/badge.svg)
![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-order-service.yml/badge.svg)
![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/brewery-config-server.yml/badge.svg)
![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/brewery-eureka.yml/badge.svg)
![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/brewery-gateway.yml/badge.svg)

Brewery Microservices
======================================================================================================================
A sample microservices project running on Spring Boot / Spring Cloud / MySQL / Artemis JMS and Docker.


This project simulates functionality of an order management / inventory management system of a "brewery" using a
microservices architecture. 


It consists of three primary services:
- [beer-service](./beer-service/README.md) - simulates the beer brewing side of the brewery. It listens on
a message queue for requests to brew more beer, "brews beer", and then notifies the
beer-inventory-service that more beer has been brewed. It will periodically call the beer-inventory-service to check
how much beer is on hand and then brew more beer if the inventory is below a certain threshold. 
This service also validates beer orders from the beer-order-service by verifying the UPC code for each beer in the order.
- [beer-inventory-service](./beer-inventory-service/README.md) - maintains the brewery's inventory of beer. It listens
for "new-inventory" events from the beer-service and updates its count of existing beer inventory. It also allocates
and deallocates inventory based on orders coming in from the beer-order-service
- [beer-order-service](./beer-order-service) - simulates a beer tasting room by placing an order for a random amount
of beer every two seconds. This is the main orchestrator of the three microservices. It uses Spring State Machine 
to keep track of order state as it moves through the services.


Plus many supporting technologies:
- [brewery-eureka](./brewery-eureka/README.md) - service discovery server using Netflix Eureka
- [brewery-config-server](./brewery-config-server/README.md) - service configuration server using Spring Cloud Config
- [brewery-gateway](./brewery-gateway/README.md) - an API Gateway Server using Spring Cloud Gateway
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign) - a declarative REST Client using OpenFeign 
- [Spring Cloud Circuit Breaker](https://spring.io/projects/spring-cloud-circuitbreaker) - provides failover functionality for all services using the circuit breaker pattern
- [Spring Cloud Sleuth Zipkin](https://spring.io/projects/spring-cloud-sleuth) - provides distributed tracing for services using [zipkin](https://zipkin.io/)
- [Spring State Machine](https://spring.io/projects/spring-statemachine) - keeps track of the current state of a beer order across services
- [ActiveMQ Artemis](https://activemq.apache.org/components/artemis/) is used as the message broker.


MySQL is used as the database provider with each service's data stored in a separate schema, and a separate DB user.



## Running Locally
You should have installed locally: Java 17+, maven 3+, Docker and docker-compose.
At least 8 gigs of ram available to Docker as it will try to start 10 containers.


The easiest way to run is via Docker using the provided [docker-compose](./docker-compose.yml) file:
> docker-compose -f ./docker-compose.yml up


Give docker about a minute to bring up the containers and to get in sync.  Eventually you should see in the docker logs 
that the beer order service is placing an order for a random beer every two seconds.

You can view the order transactions as the move through the services using the locally running [zipkin web console](http://localhost:9411).
When the zipkin web console appears, click the blue `run query` button, and you should see a list of all completed sagas.

The artemis JMS console is available at: `http:localhost:8161` using an userid and password of `artemis`


## default usernames and passwords
| Service                    | username   | password         |
|----------------------------|------------|------------------|
| MySQL admin account        | root       | password         |
| eureka web console         | netflix    | eureka           |
| config server              | MyUserName | MySecretPassword |
| inventory service REST API | good       | beer             |
| artemis management console | artemis    | artemis          |



## Default Port Mappings
| Service Name                    | Port |
|---------------------------------|------|
| Beer Service                    | 8080 |
| Beer Order Service              | 8081 |
| Beer Inventory Service          | 8082 |
| Beer Inventory Failover Service | 8083 |
| Brewery Gateway                 | 9090 |
| Brewery Eureka                  | 8761 |
| Brewery Configuration Server    | 8888 |

