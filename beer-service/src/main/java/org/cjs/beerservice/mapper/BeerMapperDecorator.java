package org.cjs.beerservice.mapper;

import org.cjs.beerservice.domain.Beer;
import org.cjs.beerservice.service.inventory.BeerInventoryService;
import org.cjs.brewery.model.events.BeerDto;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Decorates the BeerMapper with additional functionality to retrieve quantityOnHand data from
 * a beer inventory service.
 * An Abstract class is used as per MapStruct's recommendation. Additionally, the Mapper
 * interface, BeerMapper, uses the "@DecoratedWith" annotation to specify this class as the decorator
 */
public abstract class BeerMapperDecorator implements BeerMapper {
    private BeerInventoryService beerInventoryService;
    private BeerMapper mapper;


    // setter injection is used because MapStruct requires a NoArg constructor
    @Autowired
    public void setBeerInventoryService(BeerInventoryService beerInventoryService) {
        this.beerInventoryService = beerInventoryService;
    }

    @Autowired
    public void setMapper(BeerMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public BeerDto beerToBeerDtoWithInventory(Beer beer) {
        BeerDto dto = mapper.beerToBeerDto(beer);
        dto.setQuantityOnHand(beerInventoryService.getOnHandInventory(beer.getId()));
        return dto;
    }

    @Override
    public BeerDto beerToBeerDto(Beer beer) {
        return mapper.beerToBeerDto(beer);
    }

    @Override
    public Beer beerDtoToBeer(BeerDto beerDto) {
        return mapper.beerDtoToBeer(beerDto);
    }
}
