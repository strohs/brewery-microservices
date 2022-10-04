package org.cjs.beer.inventory.service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.inventory.service.config.JmsConfig;
import org.cjs.brewery.model.events.AllocateOrderRequest;
import org.cjs.brewery.model.events.AllocateOrderResult;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * listens for incoming allocation requests and attempts to allocate inventory to a beer order.
 * One of three "states" can happen during allocation:
 * 1. the order was allocated successfully because there was enough inventory to meet the requested amount
 * 2. the order could not be allocated because there was not enough inventory on hand to fulfill the requested amount
 * in this case the pending inventory flag will be set to true on the AllocateOrderResult
 * 3. some unexpected error condition occurred while attempting to allocate inventory. The allocationError flag will
 * be set on the AllocateOrderResult
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationListener {
    private final AllocationService allocationService;
    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest request){
        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();
        builder.beerOrderDto(request.getBeerOrderDto());

        log.debug("Received allocation request: {}", request.getBeerOrderDto().getId());
        request.getBeerOrderDto().getBeerOrderLines().forEach(line -> {
            log.debug("  {} order line  requested amount: {}", request.getBeerOrderDto().getId(), line.getOrderQuantity());
        });

        try{
            Boolean allocationResult = allocationService.allocateOrder(request.getBeerOrderDto());

            builder.pendingInventory(!allocationResult);

            builder.allocationError(false);
        } catch (Exception e){
            log.error("Allocation failed for Order Id:" + request.getBeerOrderDto().getId());
            builder.allocationError(true);
        }

        jmsMessageService.sendJmsMessage(
                JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                builder.build(),
                AllocateOrderResult.class.getSimpleName()
        );

//        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
//                builder.build());

    }
}
