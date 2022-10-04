package org.cjs.beer.order.service.sm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.domain.BeerOrder;
import org.cjs.beer.order.service.domain.BeerOrderEventEnum;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.repositories.BeerOrderRepository;
import org.cjs.beer.order.service.services.BeerOrderManagerImpl;
import org.cjs.beer.order.service.services.BeerOrderSaver;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


/**
 * This interceptor saves the current state of the beerOrder, i.e. its orderStatus, to the repository
 * before every state change
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderSaver beerOrderSaver;

    @Transactional
    @Override
    public void preStateChange(
            State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
            Message<BeerOrderEventEnum> message,
            Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
            StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine,
            StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> root
    ) {
        log.debug("--- Pre-State-Change ({})", state.getId());

        Optional.ofNullable(message)
                .flatMap(msg ->
                        Optional.ofNullable(
                                (String) msg.getHeaders()
                                        .getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, " ")
                        )
                )
                .ifPresent(orderId -> {
                    beerOrderSaver.saveWithNewState(UUID.fromString(orderId), state.getId());
                    //log.debug("   Saving state for order id: " + orderId + " Status: " + state.getId());
                    //BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(UUID.fromString(orderId));
                    //beerOrderSaver.saveWithNewState(beerOrder, state.getId());
                    //log.debug("   SAVED  state for order id: " + orderId + " Status: " + state.getId());
                });
    }
}
