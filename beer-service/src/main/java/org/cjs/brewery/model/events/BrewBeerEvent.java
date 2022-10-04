package org.cjs.brewery.model.events;


import lombok.NoArgsConstructor;

/**
 * This event represents a request for the beer service
 * to "brew more beer".
 */
@NoArgsConstructor
public class BrewBeerEvent extends BeerEvent {

    public BrewBeerEvent(BeerDto beerDto) {
        super(beerDto);
    }
}
