package org.cjs.beer.order.service.services;

import org.cjs.beer.order.service.domain.BeerOrder;
import org.cjs.brewery.model.BeerOrderDto;

import java.util.UUID;

/**
 * The beer order manager "manages" the different states of a beer order.
 * From order creation to order pickup, and any states in between.
 * The implementation of this class is responsible for maintaining the state machine and sending
 * events into it in order to trigger transitions to the next state and also performing any required
 * actions for a transition
 */
public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void processValidationResult(UUID beerOrderId, Boolean isValid);

    void beerOrderAllocationPassed(BeerOrderDto beerOrder);

    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrder);

    void beerOrderAllocationFailed(BeerOrderDto beerOrder);

    void beerOrderPickedUp(UUID id);

    void cancelOrder(UUID id);
}
