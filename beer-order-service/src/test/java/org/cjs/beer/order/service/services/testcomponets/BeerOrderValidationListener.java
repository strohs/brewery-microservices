package org.cjs.beer.order.service.services.testcomponets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.config.JmsConfig;
import org.cjs.beer.order.service.services.JmsMessageService;
import org.cjs.brewery.model.events.ValidateOrderRequest;
import org.cjs.brewery.model.events.ValidateOrderResult;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * An integration test helper that will listen for events on the VALIDATE-ORDER-QUEUE
 * and simulate order validation, which is done by the BeerService in a real production environment.
 * The "customerRef" property in the BeerOrderDto is used to force a failed validation by setting its
 * value to "fail-validation" otherwise validation will always pass
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {
    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void list(Message msg){
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        //condition to fail validation
        if (request.getBeerOrder().getCustomerRef() != null) {
            if (request.getBeerOrder().getCustomerRef().equals("fail-validation")){
                isValid = false;
            } else if (request.getBeerOrder().getCustomerRef().equals("dont-validate")){
                sendResponse = false;
            }
        }

        if (sendResponse) {
            jmsMessageService.sendJmsMessage(
                    JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder()
                            .isValid(isValid)
                            .orderId(request.getBeerOrder().getId())
                            .build(),
                    ValidateOrderResult.class.getSimpleName()
            );
        }
    }
}
