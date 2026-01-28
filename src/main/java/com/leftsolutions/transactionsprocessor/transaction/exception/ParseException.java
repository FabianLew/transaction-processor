package com.leftsolutions.transactionsprocessor.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;

public class ParseException extends ResponseStatusException {

    private static final String MSG_MISSING_FIELD = "Line %d: missing or empty field: %s";
    private static final String MSG_MISSING_HEADER = "Missing or wrong header: %s";
    private static final String MSG_INVALID_DATE_FORMAT = "Line %d Invalid date format: %s (expected YYYY-MM-DD)";
    private static final String MSG_INVALID_AMOUNT_FORMAT = "Line %d Invalid amount format: %s (expected decimal number)";
    private static final String MSG_DATE_NOT_IN_EXPECTED_MONTH = "Line %d: date not in expected month: %s";
    private static final String MSG_INVALID_IBAN_FORMAT = "Line %d: invalid IBAN format";
    private static final String MSG_INVALID_CURRENCY = "Line %d: invalid currency (expected ISO-4217, e.g. PLN)";
    private static final String MSG_INVALID_CATEGORY = "Line %d: invalid category (must be non-empty and <= 100 characters)";
    private static final String MSG_AMOUNT_MUST_BE_NON_ZERO = "Line %d: amount must be non-zero";

    private ParseException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static ParseException missingField(long recordNumber, String fieldName) {
        return new ParseException(String.format(MSG_MISSING_FIELD, recordNumber, fieldName));
    }

    public static ParseException invalidDateFormat(int lineNumber, String dateString) {
        return new ParseException(String.format(MSG_INVALID_DATE_FORMAT, lineNumber, dateString));
    }

    public static ParseException invalidAmountFormat(int lineNumber, String amountString) {
        return new ParseException(String.format(MSG_INVALID_AMOUNT_FORMAT, lineNumber, amountString));
    }

    public static ParseException dateNotInExpectedMonth(int lineNumber, YearMonth expectedMonth) {
        return new ParseException(String.format(MSG_DATE_NOT_IN_EXPECTED_MONTH, lineNumber, expectedMonth));
    }

    public static ParseException invalidIbanFormat(int lineNumber) {
        return new ParseException(String.format(MSG_INVALID_IBAN_FORMAT, lineNumber));
    }

    public static ParseException invalidCurrency(int lineNumber) {
        return new ParseException(String.format(MSG_INVALID_CURRENCY, lineNumber));
    }

    public static ParseException invalidCategory(int lineNumber) {
        return new ParseException(String.format(MSG_INVALID_CATEGORY, lineNumber));
    }

    public static ParseException amountMustBeNonZero(int lineNumber) {
        return new ParseException(String.format(MSG_AMOUNT_MUST_BE_NON_ZERO, lineNumber));
    }

    public static ParseException missingHeader(String field) {
        return new ParseException(String.format(MSG_MISSING_HEADER, field));
    }
}