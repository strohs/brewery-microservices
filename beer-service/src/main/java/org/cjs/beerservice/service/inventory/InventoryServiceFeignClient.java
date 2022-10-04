package org.cjs.beerservice.service.inventory;

import org.cjs.beerservice.config.FeignClientConfig;
import org.cjs.beerservice.service.inventory.model.BeerInventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.UUID;

/**
 * Uses OpenFeign, which will use service discovery and make requests to the inventory service's REST Api.
 * The fallback property is the name of the CLASS that implements the fallback feign client interface
 */
@FeignClient(
        name = "inventory-service",
        fallback = BeerInventoryServiceFailoverFeign.class,
        configuration = FeignClientConfig.class)
public interface InventoryServiceFeignClient {

    @RequestMapping(method = RequestMethod.GET, value = BeerInventoryServiceRestTemplateImpl.INVENTORY_PATH)
    ResponseEntity<List<BeerInventoryDto>> getOnHandInventory(@PathVariable UUID beerId);

}