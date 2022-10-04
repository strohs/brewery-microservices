package org.cjs.beer.order.service.services;

import lombok.extern.slf4j.Slf4j;
import org.cjs.beer.order.service.domain.BeerOrder;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.repositories.BeerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Saves a Beer Order to the repository using a new transaction.
 * This class prevents timing issues when trying to save a beer's order state multiple times
 * within an existing transaction
 */
@Slf4j
@Component
public class BeerOrderSaver {

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public BeerOrder saveWithNewState(BeerOrder beerOrder, BeerOrderStatusEnum newState) {
        beerOrder.setOrderStatus(newState);
        return beerOrderRepository.saveAndFlush(beerOrder);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public BeerOrder saveWithNewState(UUID orderId, BeerOrderStatusEnum newState) {
        BeerOrder beerOrder = beerOrderRepository.findById(orderId).get();
        log.debug("--- {} SAVED-ORDER-STATE oldState: {}", orderId, beerOrder.getOrderStatus());
        beerOrder.setOrderStatus(newState);
        BeerOrder savedOrder = beerOrderRepository.saveAndFlush(beerOrder);

        log.debug("--- {} SAVED-ORDER-STATE newState: {}   v:{}", orderId, savedOrder.getOrderStatus(), savedOrder.getVersion());
        return savedOrder;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public BeerOrder getMostCurrentState(UUID orderId) {
        return beerOrderRepository.findById(orderId).get();
    }

}
