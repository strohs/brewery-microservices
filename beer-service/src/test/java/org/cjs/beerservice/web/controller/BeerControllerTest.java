package org.cjs.beerservice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cjs.beerservice.service.BeerService;
import org.cjs.beerservice.util.BeerTester;
import org.cjs.brewery.model.events.BeerDto;
import org.cjs.beerservice.web.model.BeerStyleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
//@AutoConfigureRestDocs(uriScheme = "https", uriHost = "dev.spring.org", uriPort = 80) // ex. of customizing host parameters
@WebMvcTest(BeerController.class)
//@ComponentScan(basePackages = "org.cjs.beerservice.mapper")
class BeerControllerTest implements BeerTester {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

    @Test
    void getBeerById() throws Exception {
        BeerDto dto = getExistingBeer();
        given(beerService.getById(any(), anyBoolean())).willReturn(dto);

        // {beerId} is explicitly listed here so that RestDocs can gen the docs
        // the .param() method would be used to document Query Parameters
        mockMvc.perform(get("/api/v1/beer/{beerId}", dto.getId())
                        //.param("iscold", "yes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("v1/beer-get",
                        pathParameters (
                                parameterWithName("beerId").description("UUID of beer to get")
                        )
                        ,
//                        requestParameters (
//                                parameterWithName("iscold").description("Is Beer cold query param")
//                        )
//                        ,
                        responseFields(
                                fieldWithPath("id").description("ID of beer"),
                                fieldWithPath("version").description("version number"),
                                fieldWithPath("createdDate").description("beer creation date"),
                                fieldWithPath("lastModifiedDate").description("last time any beer field was changed"),
                                fieldWithPath("beerName").description("beer name"),
                                fieldWithPath("beerStyle").description("beer style"),
                                fieldWithPath("upc").description("universal product code"),
                                fieldWithPath("price").description("beer price in USD"),
                                fieldWithPath("quantityOnHand").description("beer quantity on hand")
                        )
                ));
    }

    @Test
    void testSaveNewBeer() throws Exception {
        BeerDto newBeerDto = getNewBeer();
        BeerDto savedBeerDto = getSavedBeer();

        given(beerService.saveNewBeer(any())).willReturn(savedBeerDto);

        String newBeerJson = objectMapper.writeValueAsString(newBeerDto);

        // requires the static class ConstrainedFields (see bottom of this class)
        ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

        mockMvc.perform(post("/api/v1/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBeerJson))
                .andExpectAll(
                        content().contentType(MediaType.APPLICATION_JSON),
                        status().isCreated(),
                        jsonPath("$.id").isNotEmpty()
                )
                .andDo(document("v1/beer-post",
                        requestFields(
                                fields.withPath("id").ignored(),
                                fields.withPath("version").ignored(),
                                fields.withPath("createdDate").ignored(),
                                fields.withPath("lastModifiedDate").ignored(),
                                fields.withPath("beerName").description("beer name"),
                                fields.withPath("beerStyle").description("beer style"),
                                fields.withPath("upc").description("universal product code"),
                                fields.withPath("price").description("beer price in USD"),
                                fields.withPath("quantityOnHand").ignored()
                        )
                ));
    }

    @Test
    void testSaveNewBeer_with_id_passed_in_dto_should_fail_validation_and_return_400() throws Exception {

        //given(beerService.saveNewBeer(any())).willReturn(savedBeer);
        // new beer
        String postedBeer = objectMapper.writeValueAsString(getExistingBeer());

        mockMvc.perform(post("/api/v1/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postedBeer))
                .andExpectAll(
                        content().contentType(MediaType.APPLICATION_JSON),
                        status().isBadRequest()
                );
    }

    @Test
    void testSaveNewBeer_with_blank_name_should_fail_validation_and_return_400() throws Exception {
        BeerDto newBeer = getNewBeer();
        newBeer.setBeerName("");
        String postedBeer = objectMapper.writeValueAsString(newBeer);

        mockMvc.perform(post("/api/v1/beer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postedBeer))
                .andExpectAll(
                        content().contentType(MediaType.APPLICATION_JSON),
                        status().isBadRequest()
                );
    }

    @Test
    void updateBeerById() throws Exception {
        BeerDto existingBeerDto = getExistingBeer();
        UUID existingId = existingBeerDto.getId();
        existingBeerDto.setId(null);
        existingBeerDto.setCreatedDate(null);
        existingBeerDto.setLastModifiedDate(null);
        existingBeerDto.setBeerStyle(BeerStyleEnum.GOSE);

        given(beerService.saveNewBeer(any())).willReturn(existingBeerDto);

        String beerJson = objectMapper.writeValueAsString(existingBeerDto);

        mockMvc.perform(put("/api/v1/beer/" + existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(beerJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateBeer_with_blank_beerName_should_return_400() throws Exception {
        BeerDto existingBeer = getExistingBeer();
        UUID existingId = existingBeer.getId();
        existingBeer.setId(null);
        existingBeer.setCreatedDate(null);
        existingBeer.setLastModifiedDate(null);
        existingBeer.setBeerName(null);
        String beerJson = objectMapper.writeValueAsString(existingBeer);

        mockMvc.perform(put("/api/v1/beer/" + existingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(beerJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }


    // uses reflection to pull in constraint restrictions from JSR-303 annotated beans and
    // automatically adds them to the generated RestDocs
    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

        ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }
}