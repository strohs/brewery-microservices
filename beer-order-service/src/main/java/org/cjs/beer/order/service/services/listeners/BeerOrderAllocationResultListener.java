package org.cjs.beer.order.service.services.listeners;

import org.cjs.beer.order.service.config.JmsConfig;
import org.cjs.beer.order.service.services.BeerOrderManager;
import org.cjs.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult result){
        log.debug("--> {} ALLOC-RES-LISTENER got result. error? {} pending inventory? {}", result.getBeerOrderDto().getId(), result.getAllocationError(), result.getPendingInventory());
        if(!result.getAllocationError() && !result.getPendingInventory()){
            //allocated normally
            log.debug("--- {} ALLOC-NORMAL ", result.getBeerOrderDto().getId());
            beerOrderManager.beerOrderAllocationPassed(result.getBeerOrderDto());
        } else if(!result.getAllocationError() && result.getPendingInventory()) {
            //pending inventory
            beerOrderManager.beerOrderAllocationPendingInventory(result.getBeerOrderDto());
        } else if(result.getAllocationError()){
            //allocation error
            beerOrderManager.beerOrderAllocationFailed(result.getBeerOrderDto());
        }
    }

}
