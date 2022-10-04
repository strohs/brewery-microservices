package org.cjs.beerservice.service.inventory;

import java.util.UUID;

public interface BeerInventoryService {

    /**
     * return the amount of inventory on hand for the given beer id
     * @param beerId
     * @return
     */
    Integer getOnHandInventory(UUID beerId);
}
