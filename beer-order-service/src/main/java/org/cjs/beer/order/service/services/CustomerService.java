package org.cjs.beer.order.service.services;

import org.cjs.brewery.model.CustomerPagedList;
import org.springframework.data.domain.Pageable;

/**
 * Handles the business logic for customers that can order beer.
 * In this service, we only keep track of the customer name, any orders they have placed, and an api key.
 * Ideally, we could probably split this into a separate microservice if we were to add more
 * functionality to customers, like more CRUD ops etc...
 */
public interface CustomerService {

    CustomerPagedList listCustomers(Pageable pageable);

}
