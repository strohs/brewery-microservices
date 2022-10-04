package org.cjs.beerservice.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.cjs.beerservice.service.inventory.model.BeerInventoryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * This implementation of the BeerInventoryService uses RestTemplate to fetch inventory data
 */
@Profile("!local-discovery")
@Slf4j
@Service
@ConfigurationProperties(prefix = "org.cjs")
public class BeerInventoryServiceRestTemplateImpl implements BeerInventoryService {

    public static final String INVENTORY_PATH = "/api/v1/beer/{beerId}/inventory";
    private final RestTemplate restTemplate;

    // hostname and port to the inventory service
    private String beerInventoryServiceHost;

    public void setBeerInventoryServiceHost(String beerInventoryServiceHost) {
        this.beerInventoryServiceHost = beerInventoryServiceHost;
    }

    public BeerInventoryServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder,
                                                @Value("${org.cjs.inventory-user}") String inventoryUser,
                                                @Value("${org.cjs.inventory-password}")String inventoryPassword
    ) {
        this.restTemplate = restTemplateBuilder
                .basicAuthentication(inventoryUser, inventoryPassword)
                .build();
    }

    @Override
    public Integer getOnHandInventory(UUID beerId) {

        // ParameterizedTypeReference is the type of the response
        // beerId is set in the URL path during the request
        ResponseEntity<List<BeerInventoryDto>> responseEntity = restTemplate
                .exchange(beerInventoryServiceHost + INVENTORY_PATH,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {},
                        beerId);

        //sum from inventory list
        Integer onHand = Objects.requireNonNull(responseEntity.getBody())
                .stream()
                .mapToInt(BeerInventoryDto::getQuantityOnHand)
                .sum();

        return onHand;
    }
}
