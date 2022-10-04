package org.cjs.brewery.model.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cjs.beerservice.web.model.BeerStyleEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDto implements Serializable {

    static final long serialVersionUID = 3251679994464211840L;

    // this is set to @Null so that clients don't send a value for this
    @Null
    private UUID id;

    @Null
    private Long version;

    @Null
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ", shape=JsonFormat.Shape.STRING)
    private OffsetDateTime createdDate;

    @Null
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ", shape=JsonFormat.Shape.STRING)
    private OffsetDateTime lastModifiedDate;

    @NotBlank(message = "beer name must not be blank")
    private String beerName;

    @NotNull(message = "beer style must not by null")
    private BeerStyleEnum beerStyle;

    @NotNull(message = "beer upc must not be null")
    //@NotNull(message = "{upc.notnull}")
    private String upc;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Positive
    @NotNull(message = "beer price must not be null")
    private BigDecimal price;

    private Integer quantityOnHand;

}