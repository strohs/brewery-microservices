package org.cjs.beer.order.service.sm;

import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.domain.BeerOrderEventEnum;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;


@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory
public class BeerOrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> validateOrderAction;
    private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> allocateOrderAction;
    private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> validationFailureAction;
    private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> allocationFailureAction;
    private final Action<BeerOrderStatusEnum, BeerOrderEventEnum> deallocateOrderAction;

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states) throws Exception {
        states.withStates()
                .initial(BeerOrderStatusEnum.NEW)
                .states(EnumSet.allOf(BeerOrderStatusEnum.class))
                .end(BeerOrderStatusEnum.PICKED_UP)
                .end(BeerOrderStatusEnum.DELIVERED)
                .end(BeerOrderStatusEnum.CANCELLED)
                .end(BeerOrderStatusEnum.DELIVERY_EXCEPTION)
                .end(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                .end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_PENDING)
                    .event(BeerOrderEventEnum.VALIDATE_ORDER)
                    .action(validateOrderAction, errorAction())
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATED)
                    .event(BeerOrderEventEnum.VALIDATION_PASSED)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.CANCELLED)
                    .event(BeerOrderEventEnum.CANCEL_ORDER)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
                    .event(BeerOrderEventEnum.VALIDATION_FAILED)
                    .action(validationFailureAction, errorAction())
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_PENDING)
                    .event(BeerOrderEventEnum.ALLOCATE_ORDER)
                    .action(allocateOrderAction, errorAction())
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.CANCELLED)
                    .event(BeerOrderEventEnum.CANCEL_ORDER)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATED)
                    .event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION)
                    .event(BeerOrderEventEnum.ALLOCATION_FAILED)
                    .action(allocationFailureAction, errorAction())
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.CANCELLED)
                    .event(BeerOrderEventEnum.CANCEL_ORDER)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.PENDING_INVENTORY)
                    .event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.PICKED_UP)
                    .event(BeerOrderEventEnum.BEERORDER_PICKED_UP)
                    .and()
                .withExternal()
                    .source(BeerOrderStatusEnum.ALLOCATED).target(BeerOrderStatusEnum.CANCELLED)
                    .event(BeerOrderEventEnum.CANCEL_ORDER)
                    .action(deallocateOrderAction, errorAction());
    }

//    @Bean
//    public StateMachineListener<BeerOrderStatusEnum, BeerOrderEventEnum> listener() {
//        return new StateMachineListenerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum>() {
//            @Override
//            public void stateChanged(State<BeerOrderStatusEnum, BeerOrderEventEnum> from, State<BeerOrderStatusEnum, BeerOrderEventEnum> to) {
//                log.debug("STATE CHANGED  {} --> {} ", from.getId(), to.getId());
//            }
//        };
//    }

    @Bean
    public Action<BeerOrderStatusEnum, BeerOrderEventEnum> errorAction() {
        return new Action<BeerOrderStatusEnum, BeerOrderEventEnum>() {

            @Override
            public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
                // RuntimeException("MyError") added to context
                Exception exception = context.getException();
                log.error(exception.getMessage());
            }
        };
    }

}
