package org.cjs.beer.inventory.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjs.brewery.model.events.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jt on 2019-07-20.
 */
@Configuration
public class JmsConfig {

    public static final String NEW_INVENTORY_QUEUE = "new-inventory";
    public static final String ALLOCATE_ORDER_QUEUE = "allocate-order";
    public static final String ALLOCATE_ORDER_RESPONSE_QUEUE = "allocate-order-response";
    public static final String DEALLOCATE_ORDER_QUEUE = "deallocate-order";

    /**
     * This default JMS Message converter will convert messages to/from JSON strings
     * and Jackson Annotated Java Objects
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        // these are the event types are being put on the queues
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(BeerEvent.class.getSimpleName(), BeerEvent.class);
        typeIdMappings.put(NewInventoryEvent.class.getSimpleName(), NewInventoryEvent.class);
        typeIdMappings.put(AllocateOrderResult.class.getSimpleName(), AllocateOrderResult.class);
        typeIdMappings.put(AllocateOrderRequest.class.getSimpleName(), AllocateOrderRequest.class);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }
}
