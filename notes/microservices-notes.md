# Microservices with Spring Boot and Spring Cloud, random Notes

## Bean Validation
- Java Bean Validation (JSR303) defines annotations for validating bean data, on properties, fields and/or methods
- JSR 380 Bean Validation 2.0
    - used in Spring Boot 2+ (and Spring Framework 5+)
    - as of spring boot 2.3+ you must import the validation libraries a separate dependency: `spring-boot-starter-validation`
    - spring uses [Hibernate Validator 6+](https://hibernate.org/validator/), which implements the Java Bean Validation 2.0 spec. plus adds Hibernates additional validators
    - hibernate will allow you to define custom validators, check their docs for info


## Customizing the Http Client library used by Spring
By default, spring will use the HttpClient implementation included with java. The HttpClient was updated in java 11 to a newer Synchronous/Asynchronous client that
supports HTTP/1.1 and HTTP/2, but there are other clients that can be used with Spring: Apache, OpenHttp...

To configure these clients for use in a template, like RestTemplate, or WebClient, you must use the appropriate Customizer interface.

Here's an example of customizing RestTemplate to use the Apache Http Client. Note that you are responsible for including the client Dependency in you POM/Gradle:

In your Maven POM:
```
<dependency>
       <groupId>org.apache.httpcomponents</groupId>
       <artifactId>httpasyncclient</artifactId>
       <version>4.1.5</version>
</dependency>
```


Then Configure a RestTemplateCustomizer
```
@Component
public class BlockingRestTemplateCustomizer implements RestTemplateCustomizer {

    private final BlockingHttpClientProperties properties;

    public BlockingRestTemplateCustomizer(BlockingHttpClientProperties properties) {
        this.properties = properties;
    }

    public ClientHttpRequestFactory clientHttpRequestFactory() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(properties.getMaxTotalConnections()); // max 100 connections
        connectionManager.setDefaultMaxPerRoute(properties.getMaxConnectionsPerRoute()); //

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(properties.getRequestTimeout()) // 3000ms
                .setSocketTimeout(properties.getSocketTimeout()) // 3000ms
                .build();

        // there are more options that could be configured, see Apache docs
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.setRequestFactory(this.clientHttpRequestFactory());
    }
}
```

## Project Lombok
- if using IntellijIDEA remember to install lombok plugin, provides  `Refactor->DeLombok` so you can view the code generated by lombok for a class
- remember to enable Annotation Processing for your project, `Settings->Build->Compiler->Annotation Processors` tick enable annotation processing


## MapStruct
- If using lombok 1.18.16+ and MapStruct in the same project, be sure to include `lombok-mapstruct-binding` depencdency
- Will also need to configure `maven-compiler-plugin` to set annotationProcessingPaths correctly. See the `BeerService` project pom

## Spring Rest Docs
- uses ASCIIDoc and annotations in your controller classes and test cases to generate documentation for your rest API
- requires some extensive? set-up in maven.pom and creating a (required) staring AsciiDoc `index.adoc` page in `src/main/asciidoc`. See BeerService
- once the docs are built, and copied to `classes/static/docs`, the generated .jar file will place the main index page at `http://localhost:8080/docs/index.html`

## Processing JSON (with Jackson)
- Jackson is most popular serializer/deserializer
- Spring Boot will autoconfigure if detected on classpath, as always, can override
- spring boot allows you to configure some jackson properties in `application.properties`

### Common Jackson Annotations
- `@JsonProperty` set the property name used for serialization/deserialization. This will override any default naming strategies
- `@JsonFormat` serialization, gives you control over how a property is serialized (i.e. dates)
- `@JsonUnwrapped` allows you to flatten a property (i.e. the properties of another Type used in your main object)
- `@JsonView` configure virtual views of objects, allows you to group properties into "views" like "Public" or "Internal"
- `@JsonManagedReference, @JsonBackReference` mapping embedded items, can handle parent/child relationships and work around loops.
- `@JsonIdentityInfo` can specify a property to determine object identity, useful for avoiding infinite recursion problems, i.e. bi-directional relationships
- `@JsonFilter` serialization, specify a programmatic property filter

### Serialization Annotations
- `@JsonAnyGetter` allows pulling the key/value pairs of a Map and serializing them as properties
- `@JsonGetter` specify a getter method to be a property
- `@JsonPropertyOrder` set the order of properties in the serialized output
- `@JsonRawValue` will serialize the string value of the property as is, **CAN BE DANGEROUS**
- `@JsonValue` indicates a single method that the library will use to serialize the entire instance.
- `@JsonRootName` creates a root element for the object
- `@JsonSerialize` allows you to specify a custom (Jackson) serializer

### Deserialization Annotations
- `@JsonCreator` tune the constructor/factory used in deserialization, very useful when we need to deserialize some JSON that doesn't exactly match the target entity we need to get.
- `@JacksonInject` indicates that a property will get its value from the injection and not from the JSON data
- `@JsonAnySetter` allows us the flexibility of using a Map as standard properties. On deserialization, the properties from JSON will simply be added to the map.
- `@JsonSetter` an alternative to @JsonProperty that marks the method as a setter method. 
This is incredibly useful when we need to read some JSON data, but the target entity class doesn't exactly match that data, and so we need to tune the process to make it fit.
- `@JsonDeserialize` indicates the use of a custom deserializer.

### Other Annotations
- `@JsonIgnoreProperties`
- `@JsonIgnore`
- `@JsonIgnoreType`
- `@JsonInclude`
- `@JsonAutoDetect`

### Testing
Spring Boot provides `@JsonTest` test slice which can be used to test your chosen Serialization/Deserialization
technology, Jackson, GSON, etc...



## Maven BOM (bill of materials)
- You can define your own maven BOM for packages that are commonly used between your services, similar to what
  spring does with its "parent" POMs; however maven BOMs are a little different:
- Helps centralize your dependencies, makes them easy to upgrade, reduces duplication
- can keep them in your version control


Maven BOMs allow you to:
- set common maven properties
- set dependency versions
- set common build profiles
- set common dependencies
- set common plugins
- set common plugin configurations
- set just about any inheritable property which is common


When using Spring Parent POMs you might create your own BOM that inherits a Parent POM, adds your dependencies and 
configurations to it, and then include your BOM in your project.

### Hosting maven artifacts
see https://jitpack.io  or you can always try to register with maven central




## Databases
common practice for microservices is that each service uses their own separate database, with its own user and 
database schema.  In the course, all services will share a single database server, but each
service will have its own user and schema. This will make it easy to migrate to separate db servers if we need to.

### tuning a connection pool
- Hikari CP is the default connection pool used in Spring Boot. Their [homepage](https://github.com/brettwooldridge/HikariCP) 
on github has useful information on connection parameters and tuning
- one (best?) way to tune connection pool parameters is to use `FlexyPool` by Vlad Mihalcea






## JMS Messaging
- messaging servers are primarily used for async messaging between services, helping to create loose coupling
- JMS is a Java API, it has many implementors
  - Apache Active MQ, Amazon SQS, RabbitMQ.....
- Why use JMS instead of just doing a direct REST call to a service?
  - messaging is asynchronous
  - greater throughput
  - can deliver messages to one or many recipients
  - JMS provides security (if needed)
  - reliability - JMS can guarantee message delivery

The course will use Apache ActiveMQ Artemis, which will eventually replace ActiveMQ "classic".


### Types of messaging
- Point to Point
  - message is queued and delivered to one consumer
  - can have multiple consumers, but message is delivered only once
  - consumers connect to a queue

- Publish / Subscribe
  - message is delivered to one or more subscribers
  - subscribers will subscribe to a topic, then receive a copy of all messages sent to the topic

### JMS Message
JMS Messages contain three parts:
- `Header` contains metadata about the message
- `Properties`, message properties are in three sections
  - `application`, the from application that is sending the message
  - `provider`, used by the JMS provider and is provider specific
  - `standard properties`, defined by the JMS API, might not be supported by the provider
- `payload`, the message itself

#### JMS Header Properties
- JMSCorrelationID - string value, set by the application, used to trace a message through multiple consumers
- JMSExpires - long value, time when message expires and gets removed from the queue, 0 = does not expire
- JMSMessageId - string value set by the JMS Provider
- JMSPriority - integer value, priority of the message
- JMSTimestamp - long, time the message was sent
- JMSType - the type of message
- JMSReplyTo - queue or topic that the sender is expecting replies on
- JMSRedelivery - boolean - has message been redelivered?
- JMSDeliveryMode - set by JMS provider for delivery mode
  - persistent (default) - JMS provider should make best effort to deliver message
  - non-persistent - occasional message loss is acceptable

#### JMS message properties
- JMSXUserId - user id sending the message, set by JMS provider
- JMSXAppId - id of the application sending the message, set by JMS provider
- JMSXDeliveryCount - number of delivery attempts, set by JSM provider
- JSMXGroupId - the message group which the message is a part of, set by client
- JSMXGroupSeq - sequence number of the message in the group, set by client
- JSMXProducerTXID - transaction id when message was produced, set by JMS Producer
- JSMXConsumerTXID - transaction id when message was consumed, set by JMS Provider
- JMSXRcvTimestamp - timestamp when message delivered to consumer, set by JMS Provider
- JMSXState - (int) state of the JMS message, set by JMS Provider

#### JMS Custom Properties
- the JMS client can set custom properties on messages
- properties are set as key/value pairs (String, value), where values must be one of:
  - String, boolean, byte, double, float, int, short, long or Object

#### JMS Provider Properties
- a JMS Client can also set JMS provider specific properties
- provider specific properties allow the client to utilize features specific to the JMS Provider
- Refer to your provider documentation for details

#### JMS Message Types
- `Message` - just a message, no payload, often used to notify about events
- `BytesMessage` - payload is an array of bytes
- `TextMessage` - message is stored as a string (often JSON or XML)
- `StreamMessage` - sequence of Java primitives
- `MapMessage` - message is name value pairs
- `ObjectMessage` - message is a serialized java object




## Sagas
"as the number of microservices grows, so does the complexity"

many challenges with a microservices:
- business transactions often span multiple services
- ACID transactions are not an option between services
- distributes transactions / two phase commits
  - complex and do not scale
- microservices should be technology-agnostic
  - this makes two phase commits event more difficult to implement


- Sagas are used to address the challenges faced when operating in a distributed environment.
- Sagas are a tool used to coordinate a series of steps for a business transaction across multiple services
- Sagas not only prescribe the series of necessary steps, they also maintain system integrity


### CAP
**Consistency, Availability, Partition Tolerance**
- `Consistency` - every read will have the most recent write
- `Availability` - each read will get a response, but without the guarantee that data is most recent write
- `Partition Tolerance` - system continues in lieu of communication errors or delays

- `CAP Theorem` - states that a distributes system can only maintain two of three


### BASE (an ACID alternative)
**Basically Available, Soft state, Eventually Consistent**
- the opposite of ACID
- coined by Dan Pritchett of EBay in 2008

- `Basically Available` - build the system to support partial failures
  - loss of some functionality vs. total system loss
- `Soft state` - transactions cascade across nodes, it can be inconsistent for a period of time
- `Eventually consistent` - when processing is complete, sytem will be consistent


### Feral Concurrency Control
application level mechanisms for maintaining database integrity
- each application is responsible for enforcing constraints (on a database)
- not available when using distributed systems



### Sagas
- a series of steps to complete a business process
- sagas coordinate the invocation of microservices via messages or requests
- sagas become the transaction model
- each step of the saga can be considered a request
- every step of the saga has a compensating transaction (request) should an error occur in any one step of the saga
  - semantically undoes the effect of the request
  - might not restore to the exact previous state - but effectively the same

#### Saga Steps
- each step should be a message or event to be consumed by a microservice
- steps are asynchronous
- within a microservice, its normal to use traditional database transactions
- each message (request) should be idempotent
  - meaning if same message/event is sent, there is no adverse effect on system state
- each step has a [compensating transaction]() to undo the actions

#### Compensating Transactions
- effectively become the feral concurrency control
- are the mechanism to maintain system integrity
- should also be idempotent
- cannot abort, need to ensure proper execution
- NOT the same as rollback to the exact previous state
  - implements business logic for a failure event

#### Sagas are ACD
- A - atomic - all transactions are executed or compensated
- C - consistency
  - referential integrity within a service by the local database
  - referential integrity across services by the application, `Feral Concurrency Control`
- D - durability
  - persisted by database of each microservice

#### Sagas and BASE
- during execution of the Saga, system is in a "Soft State"
- Eventually consistent - meaning the system will be consistent at the conclusion of the Saga
  - consistency achieved via normal completion of the saga
  - in the event of an error, consistency is achieved via compensating transactions


### Saga Coordination
- two primary approaches for saga coordination
  - `Choreography` - distributed decision making. Each actor decides next steps.
  - `Orchestration` - centralized decision making. Central component decides next steps.

#### Choreography coordination
- benefits
  - simple, loosely coupled
  - good for simpler sagas
- problems
  - cyclic dependencies
  - harder to understand because logic is spread out
  - components are more complex, will need additional logic that decides what service to call next

- choreography coordination typically implemented using events
- each actor emits an event for the next steps in the saga
- requires each actor to have logic about the saga
- each actor needs to know how to perform a compensating transaction
  - thus each actor has more coupling to other system components

#### Orchestration Coordination
- benefits
  - logic is centralized and easier to understand
  - reduced coupling, better separation of concerns
- problems
  - risk of over centralization
    - i.e. business logic from multiple services leaks into the orchestrator component
    - you need to maintain focus on separation of concerns

##### Implementation
- orchestration coordination has a central component directing other actors
- central component maintains state for Saga
  - state machine
  - saga execution coordinator (a.k.a SEC)
  - event sourcing
- must take responsibility for the completion of the saga
  - i.e. persist state to the DB, use persistent message queues, etc...


### Which coordination strategy to use?
- choreography for smaller, simpler sagas
- orchestration for larger more complex sagas

### How to implement?
- typically a custom solution - wide variety of implementations
- Open source . commercial solutions are emerging
  - still fairly early and are maturing


Spring Cloud
====================================================================================================================
[main project page](https://spring.io/projects/spring-cloud/)


## Spring Cloud Gateway
To run your own gateway use the `spring-cloud-starter-gateway` dependency.

- Features
  - built on: java 8+, Spring Framework 5, Spring Boot 2, Project Reactor
  - non-blocking, HTTP 2 support, Netty
  - Dynamic Routing
  - Route Mapping on HTTP request attributes
  - Filters for HTTP requests and responses
  - uses an event driven (event loop) architecture (because it uses Netty and Project Reactor)
  - circuit breaker integration
  - Spring Cloud Discovery client integration
  - request rate limiting


Spring team was originally going to migrate to Zuul 2 but decided to write their own gateway:
- direction of zuul 2 was unclear
- concern that portions of zuul 2 would be closed source (as zuul 1 was)
- spring 5 had recently released which had reactive support
- Spring Cloud Gateway is born...


#### API Gateway Pattern
- a server that sits in front of microservice nodes and can direct client requests to them.
- It's essentially a type of reverse proxy
- typically there is an API Gateway in front of multiple load balancers, each load balancer will
  balance requests across microservice nodes

#### Gateway responsibilities (in general)
- routing / dynamic routing
- security
- rate limiting
- monitoring / logging
- blue / green deployments
- caching
- monolith strangling - the act of slowly pulling out functionality of the monolith into separate microservices

#### Types of Gateways
- hardware based (like F5 networks)
- SaaS - AWS Elastice Load Balancer
- Web Servers configured as proxies
- Developer Oriented - (Zuul) or Spring Cloud Gateway
- ... plus others
- also note that different types can be combined


#### Netflix Zuul
- Netflix open sources `Zuul` on June 12, 2013
  - supported 1,000 different client types
  - 50,000+ requests per second
  - architecture:  customer requests (from internet) -> AWS Elastic Load Balancer -> Zuul -> some netflix service

- initial problems with Zuul (version 1)
  - using Java's servlet API
    - blocking and inefficient
    - did not support HTTP 2
- September 2016 netflix moved to Zuul 2
  - non-blocking, more efficient, (uses Netty)
  - support for HTTP 2
  - zuul 2 is also open sourced





## Eureka
netflix Eureka is a service discovery and registration service, 
It is the name of the Netflix Service Discovery Server **and** Client.

See the "intro2Eureka.pdf" for a diagram of how we use eureka in the beer service

### Eureka Client
Having `spring-cloud-starter-netflix-eureka-client` on the classpath makes the app (microservice) into both a Eureka 
“instance” (that is, it registers itself) and a “client” (it can query the registry to locate other services).

- when a service starts, it registers itself with a Eureka service
  - provides host name / IP, port and service name
  - this is known as **service registration**
  - Eureka uses the client heartbeat to determine if a client is up.

#### Service Discovery
The process of discovering available service instances.
Individual microservice clients can connect to a eureka server to get location info of other microservices

- Spring Cloud OpenFeign allows for service discovery between microservices
  - OpenFeign is a Declarative REST Client:
    - Feign creates a dynamic implementation of an interface decorated with JAX-RS or Spring MVC annotations
    - OpenFeign is a "Java to HTTP client binder", you use annotations to configure it, similar to Spring Data JPA
    - once the client is configured and injected into your `@Service` class you can use it to make REST request 
    to a microservice
      - behind the scenes it will use Eureka to find the service and Ribbon to load balance requests to it

  - include `spring-cloud-starter-openfeign` to use it
  - Spring Cloud Gateway can be configured to lookup services in Eureka
    - works with ribbon to load balance requests

- Spring Cloud Gateway can also be configured to lookup services in Eureka


#### Client Config
you must configure each eureka client with the location of a eureka server and also set a application name:
- `eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/`
- `spring.application.name=MyServiceName`

HTTP basic authentication is automatically added to your eureka client in order to authenticate with a Eureka Server.
You can also configure other forms of authentication, i.e. certificates, TLS, etc...

- To disable the Eureka Discovery Client, you can set `eureka.client.enabled` to `false`
- The status page and health indicators for a Eureka instance default to `/info` and `/health` respectively


### Ribbon
Ribbon is a software load balancer, usually used within API Gateway, to balance load across microservice nodes


### Eureka Server
- include `spring-cloud-starter-netflix-eureka-server`
- use the `@EnableEurekaServer` on your main Application class to enable server functionality
- The server has a home page with a UI and HTTP API endpoints for the normal Eureka functionality under `/eureka/*`
- The server can be configured and deployed to be highly available, with each server replicating state about the 
registered services to the others





## Circuit Breaker Pattern
Problem: how do you prevent a service failure from cascading to other services?


The goal of the circuit breaker pattern is to enable recovery from errors.
You want your services to be durable, and you do not want to expose errors to your clients.
If a service is unavailable or has unrecoverable errors, then the Circuit Breaker pattern allows specifying
an alternative action, i.e. some sort of fallback behavior, either calling a different service or returning some
sort of "canned" response, etc...

### Spring Cloud Circuit Breaker
- provides abstractions across several circuit breaker implementations
  - thus your source code is not tied to a specific implementation (similar to SLF4J)
- supported implementations are:
  - Netflix Hystrix
  - Resilience4j
  - Sentinel
  - Spring Retry

### Spring Cloud Gateway Circuit Breakers
- Spring Cloud Gateway supports Hystrix and Resilience4J
- Gateway filters are used on top of the Spring Cloud Circuit Breaker APIs
- Hystrix is now in maintenance mode, so Spring recommends using Resilience4J






## Spring Cloud Config
- Every service can pull its configuration from some external configuration server(s)
- it provides a REST style API for spring services to lookup configuration values
- when spring boot apps start up, they obtain their configuration value from Spring Cloud Config server
- properties can be global and/or application specific
- properties can also be stored by spring profiles
- can also easily encrypt/decrypt properties


To enable a spring boot application as a Configuration server, use `@EnableConfigServer`
You will then need to configure the location of the repo containing your configs plus any credentials needed.
You may need to configure the uri to your service discovery server (i.e. Eureka)

### property storage options
- git (default) or SVN
- file system
- HashiCorp's Vault
- JDBC, Redis
- AWS S3
- CredHub


### Spring Cloud Config RESTful endpoints
These are the available endpoints for configuration

- `/{application}/{profile}[/{label}]`
- `/{application}-{profile}.yml`
- `/{label}/{application}-{profile}.yml`
- `/{application}-{profile}.properties`
- `/{label}/{application}-{profile}.properties`

Remember, you must specify a profile name, if there is no active profile, `default` becomes the profile name.
`label` is an optional git label (defaults to master)

#### Examples
```
curl localhost:8888/foo/development
curl localhost:8888/foo/development/master
curl localhost:8888/foo/development,db/master
curl localhost:8888/foo-development.yml
curl localhost:8888/foo-db.properties
curl localhost:8888/master/foo-db.properties
```

### spring cloud config client
- spring cloud config client will by default look for a URL property
  - `spring.cloud.config.url` - default is `http://localhost:8888`
- if using discovery client, client will look for a service called `configserver`
- Fail Fast - you can optionally configure the client to fail with an exception if config server cannot be reached

- configuration resources served as: `/application/profile/label`
  - `application` = spring application name
  - `profile` = spring active profile(s)
  - `label` = optional git label, (defaults to master) 


There are two ways to bootstrap a configuration:

- Config First 
  - when a client bootstraps, it knows and connects to directly to a config server to obtain required configuration properties
  - you have to config the uri of the configuration server, if it every changes you will need to change this uri in every app that uses it 
- Discovery First
  - on bootstrap the client connects to a discovery server
  - looks up the config server using a name and loads required properties from it


### Security in Spring Cloud Config
- spring cloud config supports property encryption / decryption of configuration properties
- it should be used for sensitive data such as passwords, etc...
- Java Cryptography Extension (JCE)
  - prior to Java 8u162 required additional setup
  - older examples will refer to download and install of JCE
  - included by default in Java 11+
  

- spring cloud config will store encrypted properties as:
  - `{cipher}<your encrypted value here>`
  - when a spring cloud config client requests an encrypted property the value is decrypted and presented to the client
  in the request, i.e. it's encrypted at rest but decrypted in flight. make sure your connections are encrypted, HTTPS, etc...
  - must set a symmetric key property in `encrypt.key` - best practice is to set this as environment variable
  - asymmetric public/private keys also supported (see docs for details)

- spring cloud config provides HTTP(S) endpoints for property encryption / decryption
  - `POST/encrypt` - will encrypt body of post
    - `http://localhost:8888/encrypt`  plain/text post body containing plaintext string
  - `POST/decrypt` - will decrypt body of post
    - `http://localhost:8888/decrypt`  plain/text post body containing encrypted string





## Distributed Tracing
Provides the tools to trace a transaction across services.

- can also be used for to monitor performance across different services
- also logging and troubleshooting


### Spring Cloud Sleuth
NOTE: spring cloud sleuth is being rebranded and moved to the [Micrometer Tracing](https://micrometer.io/docs/tracing) 
project after version 3.1.
Micrometer is a new product owned by Pivotal that uses Sleuth

- the distributed tracing tool for Spring Cloud
- it used an open source distributed tracing library called *Brave*
- generally what happens is:
  - headers on HTTP requests or messages are enhanced with trace data
  - logging in enhanced with trace data
  - optionally, trace data can be reported to *Zipkin*

#### tracing terminology
- Sleuth uses terms established by *Dapper*
  - Dapper is a distributed tracing system created by Google for their production systems
- `Span` - a basic unit of work, typically send and receive of a message
- `Trace` - a set of spans for a transaction
- `cs /sr` - client sent / server received - a.k.a the request
- `ss / cr` - server sent / client received - a.k.a. the response

#### Installing Spring Cloud Sleuth
- group::artifact id is: `org.springframework.cloud :: spring-cloud-starter-sleuth`
- `spring.zipkin.baseUrl` is used to configure the zipkin server default url
- if you want to send traces to zipkin you must also include: `spring-cloud-sleuth-zipkin`

#### Logging output example
> DEBUG [beer-service, 398534c1c4f919, 562452bc9c5, true]
- `[Appname, TraceId, SpanId, exxportable]`
- exportable is a boolean that indicates if span should be exported to Zipkin



### Zipkin Server
- [Zipkin](http://zipkin.io) is an open source project used to report distributed tracing metrics
- information can be reported to Zipkin via webservices over HTTP
  - optionally metrics can be provided via *Kafka* or *Rabbit* (plus others)
- Zipkin is a Spring MVC project
  - recommended to use a binary distribution of Docker image
  - building your own is not supported
- Zipkin uses an in-memory database during development
  - *Cassandra* or *Elasticsearch* should be used for production

#### Zipkin quick start
- via curl
  - `curl -sSL https://zipkin.io/quickstart.sh | bash -s`
  - `java -jar zipkin.jar`
- via docker
  - `docker run -d -p 9411:9411 openzipkin/zipkin`
- view traces in UI at:
  - `http://your_host:9411/zipkin/`


### Logging Considerations
- microservices typically use consolidated logging
- log data is typically exported in JSON format
- in spring boot we can configure logback to export JSON via a special config file





## Building Spring Boot Docker Images with Maven
- Running multiple docker images on a single host can be resource intensive
- can also consume alot of disk space
- recommended:
  - use 'slim' base images of an OS as much as possible
  - be aware of build layers as you build images
    - a host system only needs one copy of a layer

- which base image to use for Java
  - highly debated
  - `eclipse-temurin`  `Fabric8`  are two options
  - `eclipse-temurin:17.0.4.1_1-jdk`
  - `eclipse-temurin:17`

- Layered Builds
  - new with Spring Boot 2.3.0+
  - used in our microservices project(s)
  - services using a BOM need to use 1.0.17+
  - services not using a BOM need to use Spring Boot 2.3.0+

- best practice is to create a maven `~/.m2/settings.xml` files containing your docker-hub credentials

### Creating the docker build
- install and configure the [fabric8 docker maven plugin](https://github.com/fabric8io/docker-maven-plugin)
- `mvn clean package docker:build`


To actually push your build to dockerhub using maven
> mvn clean package docker:build docker:push




## Maven plugin to auto bump project versions
SEE: `https://medium.com/javarevisited/how-to-increment-versions-for-the-maven-build-java-project-a7596cc501c2`

too explicitly set a version:
> mvn versions:set -DnewVersion=0.0.1


to automatically bump versions:
> mvn validate -DbumpPatch
> mvn validate -DbumpMinor
> mvn validate -DbumpMajor




## Consolidated logging with ELK stack
- `ELK` all open source products, supported by a company named **elastic**
  - Elasticsearch
    - JSON based search engine based on Lucene
    - highly scalable - 100s of nodes (cloud scale)
  - Logstash
    - data processing pipeline for log data, E.T.L. tool
    - allows you to:
      - collect data from multiple sources
      - transform
      - send (often to a Elasticsearch server)
  - Kibana
    - data visualization for Elasticsearch
    - can query data and act as a dashboard
    - can also create charts, graphs and alerts
    - plus lots more.....

- `Filebeat` is a log shipper
  - moves log data to a destination server, often a logstash server
  - filebeat has the ability to do some transformations
    - thus it is possible to skip logstash and write to elasticsearch directly
  - can convert JSON logs to JSON objects for Elasticsearch




## Spring Cloud Contract
A set of tools to implement "consumer driven contracts"




# Order allocation in beer services
- validate order - call beer service, validate beer ordered
- allocate inventory - inventory service checks for available inventory
- update order with result of allocation - receive the allocation order
- order delivered - gone from the system (e.g. do something to indicate the order has been fulfilled)

Order cancellation can happen at any time, up until delivery
Order cancellation steps:
- update order status to cancelled
- if inventory allocated, release the inventory

















