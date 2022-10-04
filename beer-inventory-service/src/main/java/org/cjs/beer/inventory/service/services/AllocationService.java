package org.cjs.beer.inventory.service.services;


import org.cjs.brewery.model.BeerOrderDto;


public interface AllocationService {

    Boolean allocateOrder(BeerOrderDto beerOrderDto);

    void deallocateOrder(BeerOrderDto beerOrderDto);
}
