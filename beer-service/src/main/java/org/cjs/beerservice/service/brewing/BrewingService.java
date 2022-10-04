package org.cjs.beerservice.service.brewing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.config.JmsConfig;
import org.cjs.beerservice.domain.Beer;
import org.cjs.beerservice.mapper.BeerMapper;
import org.cjs.beerservice.repositories.BeerRepository;
import org.cjs.beerservice.service.JmsMessageService;
import org.cjs.beerservice.service.inventory.BeerInventoryService;
import org.cjs.brewery.model.events.BeerDto;
import org.cjs.brewery.model.events.BrewBeerEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * An example Brewing Service that checks for low beer inventory every 5 seconds.
 * It does this by calling the beer inventory service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrewingService {

    private final BeerRepository beerRepository;
    private final BeerInventoryService beerInventoryService;

    private final JmsMessageService jmsMessageService;
    private final BeerMapper beerMapper;

    @Scheduled(fixedRate = 5000)
    public void checkForLowInventory() {
        List<Beer> beers = beerRepository.findAll();

        for (Beer beer : beers) {
            Integer invQoh = beerInventoryService.getOnHandInventory(beer.getId());
            log.debug(String.format("%20s minOnHand = %d   Inventory = %d", beer.getBeerName(), beer.getMinOnHand(), invQoh));

            // trigger a request to brew more beer if the minimum on hand of a beer type
            // exceeds the quantity on hand in the inventory. In this example, we are triggering the
            // event to this service (e.g. BeerSerice) which is also listening to the queue
            if (beer.getMinOnHand() >= invQoh) {
                BeerDto dto = beerMapper.beerToBeerDto(beer);
                jmsMessageService.sendJmsMessage(
                        JmsConfig.BREWING_REQUEST_QUEUE,
                        new BrewBeerEvent(dto),
                        BrewBeerEvent.class.getSimpleName());

//                jmsTemplate.convertAndSend(
//                        JmsConfig.BREWING_REQUEST_QUEUE,
//                        new BrewBeerEvent(dto)
//                );
            }
        }
    }

}
