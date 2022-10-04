![example workflow](https://github.com/strohs/brewery-microservices/beer-service/actions/workflows/beer-service.yml/badge.svg)

# Brewery Microservices - Beer-Service
TODOv2.3
## High Level Overview
### Brewing Service (Beer Service?)
- Handles "brewing" of beer
- has its own DB of the different beer types that it can brew
- listens for events on `brewing-request` queue
- puts events on new-inventory queue
- will occasionally make requests to 

1. get list of beers (in beer service itself)
2. for each beer
   - get inventory quantity on hand (from `inventory service`)
   - is QoH below brew threshold? 
     - yes - send brew beer event
     - no - goto 2


#### Brew Beer Listener (event listener on queue)
 - listens on `brewing-request` queue for a "brew beer event" which is triggered by itself when inventory is low
   - then performs a "brew beer" action
   - then puts a new inventory event on `new-inventory` queue
     - inventory service listens for this event and will create a new inventory record


#### Beer Inventory Service
- listens on the `new-inventory` queue for "new inventory events" and creates a new inventory record in DB



## Port Mappings - on single host

| Service Name                    | Port |
|---------------------------------|------|
| Beer Service                    | 8080 |
| Beer Order Service              | 8081 |
| Beer Inventory Service          | 8082 |
| Beer Inventory Failover Service | 8083 |
| Brewery Gateway                 | 9090 |
| Brewery Eureka                  | 8761 |
| Brewery Configuration Server    | 8888 |

