package org.cjs.brewery.model.events;


import lombok.*;

import java.io.Serializable;

/**
 * Base event class for the types of events that this service can create or listen for.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BeerEvent implements Serializable {

    static final long serialVersionUID = 6014590780848640101L;

    public BeerDto beerDto;
}
