package org.cjs.beer.order.service.sm.actions;

import org.cjs.beer.order.service.domain.BeerOrderEventEnum;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * An example of using a State Machine Action to perform a compensating transaction for cases where
 * order validation failed.
 * In this example we are simply logging a message. Actual production system might require you to automatically
 * create a help desk ticket, or call some internal REST Api to notify someone of the failure, etc...
 *
 */
@Slf4j
@Component
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        log.error("Compensating Transaction.... Validation Failed: " + beerOrderId);
    }
}
