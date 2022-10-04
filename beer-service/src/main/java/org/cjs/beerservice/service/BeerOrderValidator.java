package org.cjs.beerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.repositories.BeerRepository;
import org.cjs.brewery.model.BeerOrderDto;
import org.cjs.brewery.model.BeerOrderLineDto;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderValidator {

    private final BeerRepository beerRepository;

    /**
     * validates the order lines of a beer order to ensure that all lines have valid upc codes
     * that exist in our database
     * @param beerOrderDto dto containing the beer order lines to be validated
     * @return true if all upc codes exist, else false if at least one code does not exist
     */
    public boolean validate(BeerOrderDto beerOrderDto) {
        log.debug("VALIDATING ORDER: " + beerOrderDto.getId());
        boolean isValid = true;

        // alternative upc lookup that uses one sql IN statement instead of a for loop
//        Set<String> upcs = beerOrderDto
//                .getBeerOrderLines()
//                .stream()
//                .map(BeerOrderLineDto::getUpc)
//                .collect(Collectors.toSet());
//
//        int foundUpcs = beerRepository.countByUpcIn(upcs);
//        log.debug("==================== FOUND UPCs: " + foundUpcs);

        for (BeerOrderLineDto beerOrderLineDto : beerOrderDto.getBeerOrderLines()) {
            if (beerRepository.findByUpc(beerOrderLineDto.getUpc()) == null) {
                log.debug(String.format(
                        "invalid beer order: %s, order line %s: upc not found: %s",
                        beerOrderDto.getId(),
                        beerOrderLineDto.getId(),
                        beerOrderLineDto.getUpc()));
                return false;
            }
        }
        return isValid;
    }
}
