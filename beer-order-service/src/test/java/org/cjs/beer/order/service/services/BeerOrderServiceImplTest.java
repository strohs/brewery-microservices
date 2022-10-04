package org.cjs.beer.order.service.services;

import org.cjs.beer.order.service.domain.BeerOrder;
import org.cjs.beer.order.service.domain.BeerOrderLine;
import org.cjs.beer.order.service.domain.BeerOrderStatusEnum;
import org.cjs.beer.order.service.domain.Customer;
import org.cjs.beer.order.service.repositories.BeerOrderRepository;
import org.cjs.beer.order.service.repositories.CustomerRepository;
import org.cjs.beer.order.service.web.mappers.BeerOrderMapper;
import org.cjs.brewery.model.BeerOrderDto;
import org.cjs.brewery.model.BeerOrderLineDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;


/**
 * Unit test for the BeerOrderServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class BeerOrderServiceImplTest {

    @Mock
    CustomerRepository customerRepository;

    @Mock
    BeerOrderRepository beerOrderRepository;

    @Mock
    BeerOrderManager beerOrderManager;

    @Mock
    BeerOrderMapper beerOrderMapper;

    @InjectMocks
    BeerOrderServiceImpl beerOrderService;


    @Test
    void testPlaceOrder_with_valid_customer_should_save_beerOrder() {
        Customer customer = createCustomer();
        BeerOrderDto beerOrderDto = createBeerOrderDto(customer.getId());
        BeerOrder beerOrder = createBeerOrder(beerOrderDto);
        BeerOrder savedBeerOrder = createBeerOrder(beerOrderDto);
        savedBeerOrder.setId(UUID.randomUUID());
        savedBeerOrder.setCustomer(customer);
        savedBeerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrderDto savedBeerOrderDto = BeerOrderDto
                .builder()
                .id(savedBeerOrder.getId())
                .orderStatus(savedBeerOrder.getOrderStatus().name())
                .customerId(savedBeerOrder.getCustomer().getId())
                .build();

        given(customerRepository.findById(any())).willReturn(Optional.of(customer));
        given(beerOrderMapper.dtoToBeerOrder(any())).willReturn(beerOrder);
        given(beerOrderManager.newBeerOrder(any())).willReturn(savedBeerOrder);
        given(beerOrderMapper.beerOrderToDto(any())).willReturn(savedBeerOrderDto);

        // when
        BeerOrderDto finalDto = beerOrderService.placeOrder(customer.getId(), beerOrderDto);

        then(customerRepository).should().findById(any(UUID.class));
        then(beerOrderMapper).should().dtoToBeerOrder(any(BeerOrderDto.class));
        then(beerOrderManager).should().newBeerOrder(any(BeerOrder.class));
        then(beerOrderMapper).should().beerOrderToDto(any(BeerOrder.class));

    }

    @Test
    void testPlaceOrder_with_null_customer_throws_RuntimeException() {
        Customer customer = createCustomer();
        BeerOrderDto beerOrderDto = createBeerOrderDto(customer.getId());

        given(customerRepository.findById(any())).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> beerOrderService.placeOrder( customer.getId(), beerOrderDto ));
    }

    BeerOrderDto createBeerOrderDto(UUID customerId) {
        BeerOrderLineDto beerOrderLineDto = BeerOrderLineDto
                .builder()
                .orderQuantity(1)
                .beerName("Mango Bobs")
                .beerId(UUID.randomUUID())
                .upc("0631234200036")
                .beerStyle("ALE")
                .quantityAllocated(0)
                .build();
        return BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .createdDate(OffsetDateTime.now())
                .beerOrderLines(List.of(beerOrderLineDto))
                .build();
    }

    Customer createCustomer() {
        return Customer.builder()
                .customerName("Tasting Room")
                .id(UUID.randomUUID())
                .build();
    }

    BeerOrder createBeerOrder(BeerOrderDto beerOrderDto) {
        Set<BeerOrderLine> beerOrderLines = new HashSet<>();
        for (BeerOrderLineDto beerOrderLineDto : beerOrderDto.getBeerOrderLines()) {
            BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                    .beerId(beerOrderLineDto.getBeerId())
                    .orderQuantity(beerOrderLineDto.getOrderQuantity())
                    .upc(beerOrderLineDto.getUpc())
                    .build();
            beerOrderLines.add(beerOrderLine);
        }
        return BeerOrder.builder()
                .beerOrderLines(beerOrderLines)
                .build();
    }
}