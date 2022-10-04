package org.cjs.beerservice.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.domain.Beer;
import org.cjs.beerservice.repositories.BeerRepository;
import org.cjs.beerservice.web.model.BeerStyleEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
@Slf4j
public class BeerLoader implements CommandLineRunner {
    // By implementing CommandLineRunner, this bean's run() method
    // is called everytime this Spring Application starts.
    // It must be marked as a Component

    public static final String BEER_1_UPC = "0631234200036";
    public static final String BEER_2_UPC = "0631234300019";
    public static final String BEER_3_UPC = "0083783375213";

    private final BeerRepository beerRepository;

    @Override
    public void run(String... args) {
        // if no beers in the repository, bootstrap some beers
        if (beerRepository.count() == 0) {
            log.warn("Beer Service repository is empty, bootstrapping 3 beers...");
            loadBeerObjects();
        }
    }

    private void loadBeerObjects() {
        Beer b1 = Beer.builder()
                .beerName("Mango Bobs")
                .beerStyle(BeerStyleEnum.IPA.name())
                .minOnHand(12)
                .quantityToBrew(200)
                .price(new BigDecimal("12.95"))
                .upc(BEER_1_UPC)
                .build();

        Beer b2 = Beer.builder()
                .beerName("Galaxy Cat")
                .beerStyle(BeerStyleEnum.PALE_ALE.name())
                .minOnHand(12)
                .quantityToBrew(200)
                .price(new BigDecimal("12.95"))
                .upc(BEER_2_UPC)
                .build();

        Beer b3 = Beer.builder()
                .beerName("Pinball Porter")
                .beerStyle(BeerStyleEnum.PALE_ALE.name())
                .minOnHand(12)
                .quantityToBrew(200)
                .price(new BigDecimal("12.95"))
                .upc(BEER_3_UPC)
                .build();

        beerRepository.save(b1);
        beerRepository.save(b2);
        beerRepository.save(b3);
    }
}
