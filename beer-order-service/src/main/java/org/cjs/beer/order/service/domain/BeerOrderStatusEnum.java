/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.cjs.beer.order.service.domain;

/**
 * These are the states that a brewery order can be in
 */
public enum BeerOrderStatusEnum {
    // new order arrives
    NEW,

    // order successfully validated
    VALIDATED,

    // order in process of being validated
    VALIDATION_PENDING,

    // order failed validation
    VALIDATION_EXCEPTION,

    // in process of getting order allocation from inventory service
    ALLOCATION_PENDING,

    // inventory allocated successfully
    ALLOCATED,

    // inventory allocation failed
    ALLOCATION_EXCEPTION,

    // order cancelled
    CANCELLED,

    // waiting for inventory to become available for the order
    PENDING_INVENTORY,

    // order picked up (by employee)
    PICKED_UP,
    DELIVERED,
    DELIVERY_EXCEPTION
}
