package org.cjs.beer.order.service.domain;

/**
 * These are the state transition events for the brewery microservices
 */
public enum BeerOrderEventEnum {

    // validate order event
    VALIDATE_ORDER,

    // cancel order event
    CANCEL_ORDER,

    // order service successfully validated the order
    VALIDATION_PASSED,

    // order validation failed
    VALIDATION_FAILED,

    // event made to the inventory service to allocate inventory for an order
    ALLOCATE_ORDER,

    // event indicating that inventory allocation was successful
    ALLOCATION_SUCCESS,

    // event indicating there is no inventory available for an order
    ALLOCATION_NO_INVENTORY,

    // event indicating inventory allocation failed due to some other (unexpected) error condition
    ALLOCATION_FAILED,

    // event indicating a beer order was picked up
    BEERORDER_PICKED_UP
}
