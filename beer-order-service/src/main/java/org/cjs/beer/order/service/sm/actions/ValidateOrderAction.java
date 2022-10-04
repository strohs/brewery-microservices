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
import org.cjs.brewery.model.events.ValidateOrderRequest;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsMessageService jmsMessageService;

    private final BeerOrderSaver beerOrderSaver;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(UUID.fromString(beerOrderId));

        jmsMessageService.sendJmsMessage(
                JmsConfig.VALIDATE_ORDER_QUEUE,
                ValidateOrderRequest.builder()
                        .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                        .build(),
                ValidateOrderRequest.class.getSimpleName());
        log.debug("--< {} Sent Validation request to queue", beerOrderId);

//        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));
//
//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//
//            jmsMessageService.sendJmsMessage(
//                    JmsConfig.VALIDATE_ORDER_QUEUE,
//                    ValidateOrderRequest.builder()
//                            .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
//                            .build(),
//                    ValidateOrderRequest.class.getSimpleName());
//            log.debug("Sent Validation request to queue for order id " + beerOrderId);
//
//        }, () -> log.error("Order Not Found for Id: " + beerOrderId + " did not sent to queue"));

    }
}
