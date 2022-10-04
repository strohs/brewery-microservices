package org.cjs.beerservice.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjs.beerservice.util.BeerTester;
import org.cjs.brewery.model.events.BeerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing BeerDto Serialization/Deserialization using Jackson
 */
@JsonTest
public class BeerDtoTest implements BeerTester {

    // can directly use the configured object mapper by Spring Boot
    @Autowired
    ObjectMapper objectMapper;

    // or JacksonTester provided by Spring Boot test, can be used with AssertJ assertions
    @Autowired
    JacksonTester<BeerDto> json;

    @Test
    void testSerializeJson() throws IOException {
        BeerDto beerDto = getNewBeer();
        String jsonStr = this.json.write(beerDto).getJson();
        System.out.println(jsonStr);

        // we can use JSON path based assertions (with AssertJ)
        assertThat(this.json.write(beerDto)).hasJsonPathStringValue("$.beerName");
        assertThat(this.json.write(beerDto)).extractingJsonPathStringValue("$.id").isNullOrEmpty();
    }

    @Test
    void testDeserializeJson() throws JsonProcessingException {
        String json = "{\"id\":null,\"version\":null,\"createdDate\":null,\"lastModifiedDate\":null,\"beerName\":\"NewBeer\",\"beerStyle\":\"LAGER\",\"upc\":\"10A\",\"price\":\"8.75\",\"quantityOnHand\":null}";

        BeerDto dto = objectMapper.readValue(json, BeerDto.class);
        System.out.println(dto);
    }

    @Test
    void testDates() throws Exception {
        BeerDto beerDto = getExistingBeer();
        String jsonStr = this.json.write(beerDto).getJson();
        System.out.println(jsonStr);

        String ser = "{\"id\":\"cf44a079-fb38-4478-b51d-aeb85e4a7896\",\"version\":null,\"createdDate\":\"2022-09-02T14:40:25-0400\",\"lastModifiedDate\":\"2022-09-02T14:40:25-0400\",\"beerName\":\"YumYum\",\"beerStyle\":\"ALE\",\"upc\":\"12B\",\"price\":\"4.75\",\"quantityOnHand\":25}";
        BeerDto dto2 = objectMapper.readValue(ser, BeerDto.class);
        System.out.println(dto2);
    }
}
