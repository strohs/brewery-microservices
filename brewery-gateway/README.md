![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/brewery-gateway.yml/badge.svg)

# Brewery Gateway Service

The purpose of this server is to showcase how to route and load-balance REST Api requests across microservice 
instances, using route matching. In essence, this API Gateway server is acting as a reverse proxy.

Additionally, the `beer-inventory-service` has been configured to use to automatically
fail over to the `beer-inventory-failover-service`. It uses spring-circuit-breaker (Resiliance4J) under the covers to
do this.

See the [LoadBalancedRoutesConfig](./src/main/java/org/cjs/gateway/config/LoadBalancedRoutesConfig.java) code for the
configuration being used.


NOTE that this server is NOT required for the other services to run, but merely a demonstration of what an actual 
API Gateway server might be used for.

## Examples
This server will be started by the [docker-compose](../docker-compose.yml) file (on port 9090 by default).
Once all other services are started you, can make REST requests to the gateway server, and it will route the requests to 
an instance of the microservice.

For example, make a GET request (using a web browser, curl, etc...) to fetch a list of all beers
(from the beer service): `http://localhost:9090/api/v1/beer`