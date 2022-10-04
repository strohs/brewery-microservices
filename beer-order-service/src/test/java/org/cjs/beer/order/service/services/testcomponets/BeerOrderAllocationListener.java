package org.cjs.beer.order.service.services.testcomponets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.config.JmsConfig;
import org.cjs.beer.order.service.services.JmsMessageService;
import org.cjs.brewery.model.events.AllocateOrderRequest;
import org.cjs.brewery.model.events.AllocateOrderResult;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * An integration test helper that will listen for events on the ALLOCATE-ORDER-QUEUE
 * and can be used to fail allocations based on the "customerRef" property in the BeerOrderDto.
 * We are essentially mocking functionality from the beer-inventory-service but also providing code
 * that can fail the allocation when needed.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg){
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;

        //set allocation error
        if (request.getBeerOrderDto().getCustomerRef() != null) {
            if (request.getBeerOrderDto().getCustomerRef().equals("fail-allocation")){
                allocationError = true;
            }  else if (request.getBeerOrderDto().getCustomerRef().equals("partial-allocation")) {
                pendingInventory = true;
            } else if (request.getBeerOrderDto().getCustomerRef().equals("dont-allocate")){
                sendResponse = false;
            }
        }

        boolean finalPendingInventory = pendingInventory;

        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (finalPendingInventory) {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
            } else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
        });

        if (sendResponse) {
            jmsMessageService.sendJmsMessage(
                    JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResult.builder()
                            .beerOrderDto(request.getBeerOrderDto())
                            .pendingInventory(pendingInventory)
                            .allocationError(allocationError)
                            .build(),
                    AllocateOrderResult.class.getSimpleName()
            );
        }
    }
}
