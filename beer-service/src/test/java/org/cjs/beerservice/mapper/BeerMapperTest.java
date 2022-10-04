package org.cjs.beerservice.mapper;

import org.cjs.beerservice.domain.Beer;
import org.cjs.beerservice.service.inventory.BeerInventoryService;
import org.cjs.brewery.model.events.BeerDto;
import org.cjs.beerservice.web.model.BeerStyleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class BeerMapperTest {

    @Autowired
    BeerMapper beerMapper;

    @Autowired
    DateMapper dateMapper;

    @MockBean
    BeerInventoryService beerInventoryService;

    Beer beer;

    BeerDto dto;

    @BeforeEach
    void setUp() {
        Timestamp createdDateTs = new Timestamp(Instant.now().toEpochMilli());
        Timestamp lastModifiedDateTs = new Timestamp(Instant.now().toEpochMilli());
        OffsetDateTime createdDateOffset = dateMapper.asOffsetDateTime(createdDateTs);
        OffsetDateTime lastModifiedOffset = dateMapper.asOffsetDateTime(lastModifiedDateTs);


        beer = Beer.builder()
                .id(UUID.randomUUID())
                .version(22L)
                .createdDate(createdDateTs)
                .lastModifiedDate(lastModifiedDateTs)
                .beerName("Yum")
                .beerStyle(BeerStyleEnum.ALE.toString())
                .upc("UPC11")
                .price(new BigDecimal("11.22"))
                .minOnHand(100)
                .quantityToBrew(200)
                .build();

        dto = BeerDto.builder()
                .version(22L)
                .createdDate(createdDateOffset)
                .lastModifiedDate(lastModifiedOffset)
                .beerName("Yum")
                .beerStyle(BeerStyleEnum.ALE)
                .upc("UPC11")
                .price(new BigDecimal("11.22"))
                .quantityOnHand(200)
                .build();
    }

    @Test
    void testBeerToBeerDto() {
        BeerDto dto = beerMapper.beerToBeerDto(this.beer);
        assertEquals(dto.getId(), beer.getId());
        assertEquals(dto.getVersion(), beer.getVersion());
        assertEquals(dto.getCreatedDate().toInstant(), beer.getCreatedDate().toInstant());
        assertEquals(dto.getLastModifiedDate().toInstant(), beer.getCreatedDate().toInstant());
        assertEquals(dto.getBeerName(), beer.getBeerName());
        assertEquals(dto.getBeerStyle().name(), beer.getBeerStyle());
        assertEquals(dto.getUpc(), beer.getUpc());
        assertEquals(dto.getPrice(), beer.getPrice());

        System.out.println("BEEER " + beer.getCreatedDate().toInstant());
        System.out.println("DTO   " + dto.getCreatedDate().toInstant());
    }

    @Test
    void testBeerDtoToBeer() {
        Beer beer = beerMapper.beerDtoToBeer(this.dto);
        assertNull(beer.getId());
        assertNull(beer.getQuantityToBrew());
        assertEquals(dto.getVersion(), beer.getVersion());
        assertEquals(dto.getCreatedDate().toInstant(), beer.getCreatedDate().toInstant());
        assertEquals(dto.getLastModifiedDate().toInstant(), beer.getCreatedDate().toInstant());
        assertEquals(dto.getBeerName(), beer.getBeerName());
        assertEquals(dto.getBeerStyle().name(), beer.getBeerStyle());
        assertEquals(dto.getUpc(), beer.getUpc());
        assertEquals(dto.getPrice(), beer.getPrice());
        System.out.println("BEEER " + beer.getCreatedDate().toInstant());
        System.out.println("DTO   " + dto.getCreatedDate().toInstant());
    }

    @Test
    @Disabled
    void testBeerToBeerDtoWithInventory_maps_quantity_on_hand() {
        // todo this test is returning either 0 or 111...randomly. May need wire mock instead
        given(beerInventoryService.getOnHandInventory(any())).willReturn(111);

        BeerDto dto = beerMapper.beerToBeerDtoWithInventory(this.beer);

        assertEquals(dto.getId(), beer.getId());
        assertEquals(dto.getVersion(), beer.getVersion());
        assertEquals(dto.getCreatedDate().toInstant(), beer.getCreatedDate().toInstant());
        assertEquals(dto.getLastModifiedDate().toInstant(), beer.getCreatedDate().toInstant());
        assertEquals(dto.getBeerName(), beer.getBeerName());
        assertEquals(dto.getBeerStyle().name(), beer.getBeerStyle());
        assertEquals(dto.getUpc(), beer.getUpc());
        assertEquals(dto.getPrice(), beer.getPrice());
        assertEquals(111, dto.getQuantityOnHand() );

    }
}