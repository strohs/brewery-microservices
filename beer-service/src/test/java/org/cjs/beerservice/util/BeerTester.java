package org.cjs.beerservice.util;

import org.cjs.brewery.model.events.BeerDto;
import org.cjs.beerservice.web.model.BeerStyleEnum;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A helper class for test cases/
 * This interface contains default methods to quickly build different BeerDto(s)
 */
public interface BeerTester {

    default BeerDto getNewBeer() {
        return BeerDto.builder()
                .beerName("NewBeer")
                .beerStyle(BeerStyleEnum.LAGER)
                .price(new BigDecimal("8.75"))
                .upc("10A")
                .build();
    }

    default BeerDto getExistingBeer() {
        return BeerDto.builder()
                .beerName("YumYum")
                .id(UUID.randomUUID())
                .beerStyle(BeerStyleEnum.ALE)
                .price(new BigDecimal("4.75"))
                .upc("12B")
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .quantityOnHand(25).build();
    }

    default BeerDto getSavedBeer() {
        return BeerDto.builder()
                .beerName("NewBeer")
                .id(UUID.randomUUID())
                .beerStyle(BeerStyleEnum.LAGER)
                .price(new BigDecimal("8.75"))
                .upc("10A")
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .quantityOnHand(25).build();
    }
}
