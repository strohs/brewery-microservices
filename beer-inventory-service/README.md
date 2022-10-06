![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-inventory-service.yml/badge.svg)

# Beer Inventory Service
Beer inventory service for the Brewery Microservices project.

This service is responsible for allocating and deallocating inventory as orders come in from the order service.
In addition, it will also create new inventory when it receives a message to do so from the beer-service


## Message Queue Endpoints

| Queue Name              | listen / send | Event Type                                          | Description                                                                              |
|-------------------------|---------------|-----------------------------------------------------|------------------------------------------------------------------------------------------|
| new-inventory           | listen        | org.cjs.brewery.model.events.NewInventoryEvent      | notifies this service that new beer has been brewed and should be added to the inventory |
| allocate-order          | listen        | org.cjs.brewery.model.events.AllocateOrderRequest   | notifies this service to try and allocate beer inventory, if available, to an order      |
| allocate-order-response | send          | org.cjs.brewery.model.events.AllocateOrderResponse  | puts the result of an AllocateOrderRequest                                               |
| deallocate-order        | listen        | org.cjs.brewery.model.events.DeallocateOrderRequest | notifies this service that the items in an order should be returned to the inventory     |


## REST Endpoints
NOTE: the endpoints of this service are secured using HTTP Basic Authentication using the following userId and password:
- userid = `good`
- password = `beer`


The following endpoints are exposed. Each endpoint returns a JSON response

| Endpoint                         | Method | params                           | Result Type                            | Description                                                   |
|----------------------------------|--------|----------------------------------|----------------------------------------|---------------------------------------------------------------|
| `api/v1/beer/{beerId}/inventory` | GET    | beerId = internal UUID of a beer | org.cjs.brewery.model.BeerInventoryDto | returns the amount of inventory on hand for the given beer id |


## Database Schema
- database name = `beersinventoryservice`
- userid = `beer_inventory_service`
- password = `password`
- table = `BeerInventory` from [BeerInventory](./src/main/java/org/cjs/beer/inventory/service/domain/BeerInventory.java)