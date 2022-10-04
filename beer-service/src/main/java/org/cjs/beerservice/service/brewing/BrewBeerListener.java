package org.cjs.beerservice.service.brewing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.config.JmsConfig;
import org.cjs.beerservice.domain.Beer;
import org.cjs.beerservice.repositories.BeerRepository;
import org.cjs.beerservice.service.JmsMessageService;
import org.cjs.brewery.model.events.BeerDto;
import org.cjs.brewery.model.events.BrewBeerEvent;
import org.cjs.brewery.model.events.NewInventoryEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens on a queue for "Brew Beer" events and then simulates brewing beer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrewBeerListener {

    private final BeerRepository beerRepository;
    private final JmsMessageService jmsMessageService;

    @Transactional
    @JmsListener(destination = JmsConfig.BREWING_REQUEST_QUEUE)
    public void listen(BrewBeerEvent beerEvent) {
        log.debug("got request to brew more beer for " + beerEvent);
        BeerDto dto = beerEvent.getBeerDto();
        Beer beer = beerRepository.findById(dto.getId()).orElseThrow();

        // simulate brewing beer by setting quantity on hand to quantity to brew
        dto.setQuantityOnHand(beer.getQuantityToBrew());

        // notify the inventory service that X amount of beer has been brewed
        log.debug(String.format("brewed some more %s, minOnHand=%d  QoH=%d", beer.getBeerName(), beer.getMinOnHand(), dto.getQuantityOnHand()));
        jmsMessageService.sendJmsMessage(
                JmsConfig.NEW_INVENTORY_QUEUE,
                new NewInventoryEvent(dto),
                NewInventoryEvent.class.getSimpleName()
        );
        //jmsTemplate.convertAndSend(JmsConfig.NEW_INVENTORY_QUEUE, newInventoryEvent);
    }
}
