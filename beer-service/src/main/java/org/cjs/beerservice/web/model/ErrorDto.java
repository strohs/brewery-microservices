package org.cjs.beerservice.web.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * A general purpose error class for cases when you need to return a status code along with
 * a list of error messages
 */
@Value
@Builder
@RequiredArgsConstructor
public class ErrorDto {

    Integer code;
    List<String> messages;

}
