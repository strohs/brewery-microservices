package org.cjs.beerservice.web.controller;

import org.cjs.beerservice.web.model.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;


@ControllerAdvice
public class MvcExceptionHandler {

    /**
     * This handler prints all validation errors as a string that gets returned to the client. A real API
     * would use a well documented, marked up JSON/XML response.
     * Note that the default messages can be configured in the validation annotations themselves, or externalized
     * in messages.properties file(s) for il8 purposes
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> validationErrorHandler(MethodArgumentNotValidException e) {
        List<String> errors = e.getFieldErrors()
                .stream()
                .map(err -> {
                    if (err.getDefaultMessage() != null) {
                        return err.getDefaultMessage();
                    } else {
                        return err.getField() + " has an error";
                    }
                })
                .toList();
        ErrorDto dto = new ErrorDto(HttpStatus.BAD_REQUEST.value(), errors);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bind exceptions occur when Spring message converters cannot deserialize a request from JSON/XML/....
     * This handler returns a list of all raw messages for each binding error that occurred... for now
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorDto> handleBindException(BindException bex) {
        List<String> errors = bex.getAllErrors().stream().map(ObjectError::toString).toList();
        ErrorDto dto = new ErrorDto(HttpStatus.BAD_REQUEST.value(), errors);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }
}
