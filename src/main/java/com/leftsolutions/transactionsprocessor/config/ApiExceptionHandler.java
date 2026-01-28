package com.leftsolutions.transactionsprocessor.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse handleDateTimeParseException(DateTimeParseException dateTimeParseException) {
        return new ApiErrorResponse(
                "INVALID_MONTH_FORMAT",
                "Expected format: yyyy-MM"
        );
    }
}
