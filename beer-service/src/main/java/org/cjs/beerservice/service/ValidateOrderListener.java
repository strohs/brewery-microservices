package org.cjs.beerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.config.JmsConfig;
import org.cjs.brewery.model.BeerOrderDto;
import org.cjs.brewery.model.events.ValidateOrderRequest;
import org.cjs.brewery.model.events.ValidateOrderResult;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * listens for incoming validate order requests, calls a service to validate them, and puts the results of
 * the validation on a response queue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderListener {

    private final JmsMessageService jmsMessageService;
    private final BeerOrderValidator beerOrderValidator;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(ValidateOrderRequest request) {
        BeerOrderDto beerOrderDto = request.getBeerOrder();
        boolean isValid = beerOrderValidator.validate(beerOrderDto);

        ValidateOrderResult result = ValidateOrderResult.builder()
                .orderId(beerOrderDto.getId())
                .isValid(isValid)
                .build();

        jmsMessageService.sendJmsMessage(
                JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                result,
                ValidateOrderResult.class.getSimpleName());
    }
}
