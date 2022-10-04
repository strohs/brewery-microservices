package org.cjs.beer.order.service.sm.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.config.JmsConfig;
import org.cjs.beer.order.service.domain.BeerOrderEventEnum;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.services.BeerOrderManagerImpl;
import org.cjs.beer.order.service.services.JmsMessageService;
import org.cjs.brewery.model.events.AllocationFailureEvent;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;


/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsMessageService jmsMessageService;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);

        jmsMessageService.sendJmsMessage(
                JmsConfig.ALLOCATE_FAILURE_QUEUE,
                AllocationFailureEvent.builder()
                        .orderId(UUID.fromString(beerOrderId))
                        .build(),
                AllocationFailureEvent.class.getSimpleName());

        log.debug("Sent Allocation Failure Message to queue for order id " + beerOrderId);
    }
}