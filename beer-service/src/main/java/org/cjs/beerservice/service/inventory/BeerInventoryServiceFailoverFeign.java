package org.cjs.beerservice.service.inventory;

import lombok.RequiredArgsConstructor;
import org.cjs.beerservice.service.inventory.model.BeerInventoryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BeerInventoryServiceFailoverFeign implements InventoryServiceFeignClient {

    private final InventoryServiceFailoverFeignClient feignClient;

    @Override
    public ResponseEntity<List<BeerInventoryDto>> getOnHandInventory(UUID beerId) {
        return feignClient.getOnHandInventory();
    }
}
