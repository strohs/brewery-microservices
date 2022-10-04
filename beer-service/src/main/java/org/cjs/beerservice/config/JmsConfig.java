package org.cjs.beerservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjs.brewery.model.events.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class JmsConfig {

    // this queue will be used to listen for BrewBeerEvents and will be used to
    // place
    public static final String BREWING_REQUEST_QUEUE = "brewing-request";

    public static final String NEW_INVENTORY_QUEUE = "new-inventory";
    public static final String VALIDATE_ORDER_QUEUE = "validate-order";
    public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";


    /**
     * This default JMS Message converter will convert messages to/from JSON strings
     * and Jackson Annotated Java Objects
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        // these are the event types are being put on the queues
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(BeerEvent.class.getSimpleName(), BeerEvent.class);
        typeIdMappings.put(BrewBeerEvent.class.getSimpleName(), BrewBeerEvent.class);
        typeIdMappings.put(NewInventoryEvent.class.getSimpleName(), NewInventoryEvent.class);
        typeIdMappings.put(ValidateOrderResult.class.getSimpleName(), ValidateOrderResult.class);
        typeIdMappings.put(ValidateOrderRequest.class.getSimpleName(), ValidateOrderRequest.class);
        

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }
}
