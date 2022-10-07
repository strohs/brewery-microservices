![example workflow](https://github.com/strohs/brewery-microservices/beer-service/actions/workflows/beer-service.yml/badge.svg)

# Beer-Service
The beer-service has three jobs:

1. maintain a database as the source of truth for all the types of beer that can be brewed by the brewery
2. validate beer order UPC codes coming from the beer-order-service
3. occasionally make a request to the inventory-service to see how much beer is "on-hand" in the inventory. If the 
amount is below a certain threshold, then more beer is "brewed" and the amount brewed is sent to the inventory-service
so that it can update its internal count


This service uses EHCache to cache the results of any GET request made to this service. Data inserted into cache
has a time-to-live of five minutes.


## Database Schema
- database name = `beerservice`
- userid = `beer_service`
- password = `password`
- table = `Beer` described in [Beer](./src/main/java/org/cjs/beerservice/domain/Beer.java)


## Message Queue Endpoints

| Queue Name              | listen / send | Event Type                                          | Description                                                                                      |
|-------------------------|---------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------|
| validate-order          | listen        | org.cjs.brewery.model.events.ValidateOrderRequest   | events on this queue will trigger the beer-service to validate the UPC code of the given order   |
| validate-order-response | send          | org.cjs.brewery.model.events.ValidateOrderResult    | the result of a UPC validation is put on this queue                                              |
| brewing-request         | listen/send   | org.cjs.brewery.model.events.BrewBeerEvent          | this service sends a message to itself as a trigger to make more beer                            |
| new-inventory           | send          | org.cjs.brewery.model.events.NewInventoryEvent      | this queue is used to notify the beer-inventory-service that some amount of beer has been brewed |



## REST Endpoints
The following endpoints are exposed. Each endpoint returns a JSON response. Any POST methods accept a JSON request.


| Endpoint                    | Method | path params              | Request Type | Response Type     | Description                                                 |
|-----------------------------|--------|--------------------------|--------------|-------------------|-------------------------------------------------------------|
| `/api/v1/beer`              | GET    | N/A                      | N/A          | BeerPagedList     | get a pageable list of all beers maintained by this service |
| `/api/v1/beer/{beerId}`     | GET    | beerId = UUID of a beer  | N/A          | BeerDto           | get info on a specific beer                                 |
| `/api/v1/beerUpc/{beerUpc}` | GET    | upc = upc code of a beer | N/A          | BeerDto           | get info on a specific beer by its upc code                 |
| `/api/v1/beer`              | POST   | N/A                      | BeerDto      | BeerDto           | create a new beer entry in the database                     |
| `/api/v1/beer/{beerId}`     | PUT    | beerId= UUID of beer     | BeerDto      | BeerDto           | update fields of an existing beer                           |


There are some (partial) REST docs available at `http://localhost:8080/docs/index.html` that were generated using
Spring Rest Docs.