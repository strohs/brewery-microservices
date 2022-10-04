package org.cjs.beerservice.service;

import org.cjs.brewery.model.events.BeerDto;
import org.cjs.beerservice.web.model.BeerPagedList;
import org.cjs.beerservice.web.model.BeerStyleEnum;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface BeerService {

    /**
     * returns a PagedList of beers, matching the given search criteria
     * @param beerName if present, only beers with the given name will be returned
     * @param beerStyle if present, only beers with the given style will be returned
     * @param pageRequest page request parameters
     * @param showInventoryOnHand if true, the returned PagedList will contain the number of beers in
     *                            our inventory that match the given beerName and/or beerStyle
     */
    BeerPagedList listBeers(String beerName, BeerStyleEnum beerStyle, PageRequest pageRequest, Boolean showInventoryOnHand);

    BeerDto getById(UUID beerId, Boolean showInventoryOnHand);

    BeerDto saveNewBeer(BeerDto beerDto);

    BeerDto updateBeer(UUID beerId, BeerDto beerDto);

    BeerDto getByUpc(String upc);
}
