package org.cjs.beer.order.service.sm.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.config.JmsConfig;
import org.cjs.beer.order.service.domain.BeerOrder;
import org.cjs.beer.order.service.domain.BeerOrderEventEnum;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.repositories.BeerOrderRepository;
import org.cjs.beer.order.service.services.BeerOrderManagerImpl;
import org.cjs.beer.order.service.services.BeerOrderSaver;
import org.cjs.beer.order.service.services.JmsMessageService;
import org.cjs.beer.order.service.web.mappers.BeerOrderMapper;
import org.cjs.brewery.model.events.DeallocateOrderRequest;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Triggers a compensating transaction that deallocates any previously allocated inventory.
 * It does this by placing a DeallocateOrderRequest on the DEALLOCATE_ORDER_QUEUE which should then get
 * picked up by the Beer-Inventory-Service
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsMessageService jmsMessageService;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final BeerOrderSaver beerOrderSaver;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(UUID.fromString(beerOrderId));
        jmsMessageService.sendJmsMessage(
                JmsConfig.DEALLOCATE_ORDER_QUEUE,
                DeallocateOrderRequest.builder()
                        .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                        .build(),
                DeallocateOrderRequest.class.getSimpleName()
        );
        log.debug("Sent Deallocation Request for order id: " + beerOrderId);


//        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));
//
//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//            jmsMessageService.sendJmsMessage(
//                    JmsConfig.DEALLOCATE_ORDER_QUEUE,
//                    DeallocateOrderRequest.builder()
//                            .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
//                            .build(),
//                    DeallocateOrderRequest.class.getSimpleName()
//            );
//
//            log.debug("Sent Deallocation Request for order id: " + beerOrderId);
//        }, () -> log.error("Beer Order Not Found!"));
    }
}
