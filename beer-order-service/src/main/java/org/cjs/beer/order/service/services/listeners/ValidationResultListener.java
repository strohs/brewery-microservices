package org.cjs.beer.order.service.services.listeners;

import org.cjs.beer.order.service.config.JmsConfig;
import org.cjs.beer.order.service.services.BeerOrderManager;
import org.cjs.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listens for incoming validation results from the BeerService
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResult result){
        final UUID beerOrderId = result.getOrderId();

        log.debug("--> {} VALID-RES-LISTENER got result", beerOrderId);

        beerOrderManager.processValidationResult(beerOrderId, result.getIsValid());
    }
}
