package org.cjs.beerservice.service.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.service.inventory.model.BeerInventoryDto;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Uses OpenFeign to make REST requests to BeerInventoryService
 */
@Profile("local-discovery")
@Slf4j
@RequiredArgsConstructor
@Service
public class BeerInventoryServiceFeign implements BeerInventoryService {

    private final InventoryServiceFeignClient inventoryServiceFeignClient;

    @Override
    public Integer getOnHandInventory(UUID beerId) {
        log.debug("Calling Inventory Service - BeerId: " + beerId);

        ResponseEntity<List<BeerInventoryDto>> responseEntity = inventoryServiceFeignClient.getOnHandInventory(beerId);

        Integer onHand = Objects.requireNonNull(responseEntity.getBody())
                .stream()
                .mapToInt(BeerInventoryDto::getQuantityOnHand)
                .sum();

        log.debug("BeerId: " + beerId + " On hand is: " + onHand);

        return onHand;
    }
}
