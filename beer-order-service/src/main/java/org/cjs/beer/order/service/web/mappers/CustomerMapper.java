package org.cjs.beer.order.service.web.mappers;

import org.cjs.beer.order.service.domain.Customer;
import org.cjs.brewery.model.CustomerDto;
import org.mapstruct.Mapper;


@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {
    CustomerDto customerToDto(Customer customer);

    Customer dtoToCustomer(Customer dto);
}
