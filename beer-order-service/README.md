![example workflow](https://github.com/strohs/brewery-microservices/actions/workflows/beer-order-service.yml/badge.svg)

# Beer Order Service

This service is responsible for simulating beer orders from the brewery's "taproom".
More importantly, this service is the orchestrator of sagas across the other microservices. 
It handles the happy path of an order, plus the possibility that an order could not be fulfilled due to not 
having enough inventory or due to a customer randomly "cancelling" their order. 

Spring State Machine is used to keep track of the [order states](./src/main/java/org/cjs/beer/order/service/domain/BeerOrderStatusEnum.java).


## Database Schema
- database name = `beerorderservice`
- userid = `beer_order_service`
- password = `password`
- table = `BeerOrder` described in [BeerOrder](./src/main/java/org/cjs/beer/order/service/domain/BeerOrder.java)


## Message Queue Endpoints

| Queue Name              | listen / send | Event Type                                          | Description                                                                                        |
|-------------------------|---------------|-----------------------------------------------------|----------------------------------------------------------------------------------------------------|
| validate-order          | send          | org.cjs.brewery.model.events.ValidateOrderRequest   | sends a request to validate the upc code of an order                                               |
| validate-order-response | listen        | org.cjs.brewery.model.events.ValidateOrderResult    | listens for the result of a validate order request                                                 |
| allocate-order          | send          | org.cjs.brewery.model.events.AllocateOrderRequest   | sends a request to allocate inventory for the given order                                          |
| allocate-order-response | listen        | org.cjs.brewery.model.events.AllocateOrderResult    | listens for and validates the result of an AllocateOrderRequest                                    |
| allocation-failure      | listen        | org.cjs.brewery.model.events.AllocationFailureEvent | listens for any allocation failures and updates the status and state of the order                  |
| deallocate-order        | send          | org.cjs.brewery.model.events.DeallocateOrderRequest | sends a request to the inventory-service to deallocate inventory for this order that was cancelled |


## REST Endpoints
The following endpoints are exposed. Each endpoint returns a JSON response, all POST methods accept a JSON request

| Endpoint                                                 | Method | params                                     | Request Type | Response Type      | Description                                               |
|----------------------------------------------------------|--------|--------------------------------------------|--------------|--------------------|-----------------------------------------------------------|
| `/api/v1/customers/{customerId}/orders`                  | GET    | customerId = UUID of the ordering customer | N/A          | BeerOrderPagedList | get a pageable list of all orders made by the customer    |
| `/api/v1/customers/{customerId}/orders`                  | POST   | customerId = UUID of the ordering customer | BeerOrderDto | BeerOrderDto       | post a new beer order to this service for the customer    |
| `/api/v1/customers/{customerId}/orders/{orderId}`        | GET    | orderId = UUID of the order to get         | N/A          | BeerOrderDto       | gets the details of an order by its orderId               |
| `/api/v1/customers/{customerId}/orders/{orderId}/pickup` | PUT    | customerId, orderId,                       | N/A          | N/A                | update the status of the given order/customer to 'PICKUP' |
|                                                          |        |                                            |              |                    |                                                           |
| `/api/v1/customers/`                                     | GET    | N/A                                        | N/A          | CustomerPagedList  | get a pageable list of brewery customers                  |
