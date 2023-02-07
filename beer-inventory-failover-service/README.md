![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-inventory-failover-service.yml/badge.svg)

# Beer Inventory Failover Service
A failover microservice for the [beer inventory service](../beer-inventory-service/README.md)

This failover service will handle any HTTP GET requests made to the inventory service should the actual beer inventory 
service's circuit breaker get tripped.

As this is just a basic exmaple of a failover service, it will return a "mock" beer object with its quantity on hand set 
to 999. This response is enough to keep the other services running smoothly as we don't want them to see any error states 
in the inventory service.

