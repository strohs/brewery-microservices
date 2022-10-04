package org.cjs.beer.order.service.services;

import org.cjs.beer.order.service.domain.BeerOrder;
import org.cjs.beer.order.service.domain.BeerOrderEventEnum;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.repositories.BeerOrderRepository;
import org.cjs.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import org.cjs.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    private final BeerOrderSaver beerOrderSaver;


    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {

        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderSaver.saveWithNewState(beerOrder, BeerOrderStatusEnum.NEW);
        log.debug("--- {} NEW order started -------------------------------------------------", savedBeerOrder.getId());
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        log.debug("--- {} PROCESS-VALIDATION-RES order valid? {}", beerOrderId, isValid);
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(beerOrderId);
        if(isValid){
            log.debug("    {} saver state: {}   v:{}", beerOrder.getId(), beerOrder.getOrderStatus(), beerOrder.getVersion());

            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            // get the latest copy of the order as it may have beem saved/modified by the StateChangeInterceptor
            BeerOrder validatedOrder = beerOrderSaver.getMostCurrentState(beerOrder.getId());
            log.debug("    {} valid state: {}   v:{}", validatedOrder.getId(), validatedOrder.getOrderStatus(), validatedOrder.getVersion());

            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);

        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }


        //awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATION_PENDING);
        //Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//            if(isValid){
//                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
//
//                //wait for status change
//                awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATED);
//                // get the latest copy of the order as it may have beem saved/modified by the StateChangeInterceptor
//                BeerOrder validatedOrder = beerOrderRepository.findById(beerOrderId).get();
//
//                sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
//
//            } else {
//                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
//            }
//        }, () -> log.error("process valid. res. Order Not Found. Id: " + beerOrderId));
    }

    /**
     * handles the case where inventory was allocated successfully to an order.
     * Repository will be updated with the newly allocated quantity, and the ALLOCATION_SUCCESS message
     * will be sent to the state machine
     * @param beerOrderDto
     */
    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(beerOrderDto.getId());

        log.debug("--- {} ALLOCATION-PASSED", beerOrder.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
        updateAllocatedQty(beerOrderDto);

//        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
//            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);
//            updateAllocatedQty(beerOrderDto);
//        }, () -> log.error("Order Id Not Found: " + beerOrderDto.getId() ));
    }

    /**
     * handles the case where inventory could not be allocated because inventory is pending.
     * The BeerOrder will be updated with whatever amount of inventory was allocated.
     * The ALLOCATION_NO_INVENTORY event will be sent to the state machine
     * @param beerOrderDto
     */
    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(beerOrderDto.getId());
        log.debug("--- {} ALLOCATION-PENDING-INVENTORY", beerOrder.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        updateAllocatedQty(beerOrderDto);

//        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
//            awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);
//            updateAllocatedQty(beerOrderDto);
//        }, () -> log.error("Order Id Not Found: " + beerOrderDto.getId() ));

    }

    /**
     * updates the allocated quantity in the repository for the given beerOrderDto.
     * The beerOrder and beerOrderLine(s) will all be updated based on the corresponding properties
     * in the given beerOrderDto
     * @param beerOrderDto
     */
    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        BeerOrder allocatedOrder = beerOrderSaver.getMostCurrentState(beerOrderDto.getId());
        log.debug("--- {} UPDATE-ALLOC-QUANT  # order lines: {}", allocatedOrder.getId(), allocatedOrder.getBeerOrderLines().size());
        allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                if(beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                    log.debug("---    {} UPDATE-ALLOC-QUANT  requested: {}  allocated: {}", beerOrderDto.getId(), beerOrderLineDto.getOrderQuantity(), beerOrderLineDto.getQuantityAllocated());
                    beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                }
            });
        });
        beerOrderRepository.saveAndFlush(allocatedOrder);

//        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
//        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
//            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
//                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
//                    if(beerOrderLine.getId().equals(beerOrderLineDto.getId())){
//                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
//                    }
//                });
//            });
//
//            beerOrderRepository.saveAndFlush(allocatedOrder);
//        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(beerOrderDto.getId());
        log.debug("--- {} ORDER-ALLOC-FAILED", beerOrder.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);

//        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
//        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()) );

    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(id);
        log.debug("--- {} ORDER-PICKED-UP", beerOrder.getId());
        //do process
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);

//        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);
//        beerOrderOptional.ifPresentOrElse(beerOrder -> {
//            //do process
//            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);
//        }, () -> log.error("Order Not Found. Id: " + id));
    }

    @Override
    public void cancelOrder(UUID id) {
        BeerOrder beerOrder = beerOrderSaver.getMostCurrentState(id);
        log.debug("--- {} ORDER-CANCELLED", beerOrder.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);

//        beerOrderRepository.findById(id).ifPresentOrElse(beerOrder -> {
//            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCEL_ORDER);
//        }, () -> log.error("Order Not Found. Id: " + id));
    }

    /**
     * sends the given eventEnum into the state machine associated with the given beerOrder.
     * This will cause the state machine to transition to a new state
     * @param beerOrder - the beer order details
     * @param eventEnum - the event to send to the state machine
     */
    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);
        log.debug("--+ {} SEND-SM-EVENT  curStatus: {}   event: {}", beerOrder.getId().toString(), beerOrder.getOrderStatus(), eventEnum);
        Message<BeerOrderEventEnum> msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        sm.sendEvent(msg);
    }

//    /**
//     * This method waits for the specified beerOrderId to reach the specified statusEnum
//     * in the beerOrderRepository. It is a BAND-AID to help the repository state catch up to the state machine
//     * state before letting the state machine transition to a new state.
//     * It was created to avoid timing issues between state machine transitions and updates in the
//     * beerOrder repository. Transitions were happening too quickly and the DB was not keeping up.
//     * @param beerOrderId
//     * @param statusEnum
//     */
//    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {
//
//        AtomicBoolean found = new AtomicBoolean(false);
//        AtomicInteger loopCount = new AtomicInteger(0);
//
//        while (!found.get()) {
//            if (loopCount.incrementAndGet() > 10) {
//                found.set(true);
//                log.error("-------------- Loop Retries exceeded -------------------");
//            }
//
//            Optional<BeerOrder> beerOrderOpt = beerOrderRepository.findById(beerOrderId);
//            if (beerOrderOpt.isPresent()) {
//                BeerOrder beerOrder = beerOrderOpt.get();
//                if (beerOrder.getOrderStatus().equals(statusEnum)) {
//                    found.set(true);
//                    log.debug("++++ Order FOUND ++++");
//                } else {
//                    log.debug("-------------------- Order Status Not Equal. Expected: " + statusEnum.name() + " Found: " + beerOrder.getOrderStatus().name());
//                }
//            } else {
//                log.debug("---- Order Id Not Found");
//            }
//
//            if (!found.get()) {
//                try {
//                    log.debug("---- Sleeping for retry");
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    // do nothing
//                }
//            }
//        }
//    }

    /**
     * builds and returns a StateMachine associated with the given beerOrder. The state machine will be
     * re-hydrated so that its current state gets set to the given beerOrder.getOrderStatus()
     * @param beerOrder
     */
    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        // stop the state machine
        sm.stop();

        // re-hydrate the state machine by setting its state to the current beer order status passed in to this
        // method. This will typically be the status that was stored in the DATABASE
        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
