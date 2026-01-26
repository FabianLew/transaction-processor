package com.leftsolutions.transactionsprocessor.transaction.exception;

public class ParseException extends RuntimeException {

    private static final String MSG_MISSING_FIELD = "Line %d: missing or empty field: %s";
    private static final String MSG_INVALID_DATE_FORMAT = "Invalid date format: %s (expected YYYY-MM-DD)";
    private static final String MSG_INVALID_AMOUNT_FORMAT = "Invalid amount format: %s (expected decimal number)";
    private static final String MSG_DATE_NOT_IN_EXPECTED_MONTH = "Line %d: date not in expected month";
    private static final String MSG_INVALID_IBAN_FORMAT = "Line %d: invalid IBAN format";
    private static final String MSG_INVALID_CURRENCY = "Line %d: invalid currency (expected ISO-4217, e.g. PLN)";
    private static final String MSG_INVALID_CATEGORY = "Line %d: invalid category (must be non-empty and <= 100 characters)";
    private static final String MSG_AMOUNT_MUST_BE_NON_ZERO = "Line %d: amount must be non-zero";

    private ParseException(String message) {
        super(message);
    }

    public static ParseException missingField(long recordNumber, String fieldName) {
        return new ParseException(String.format(MSG_MISSING_FIELD, recordNumber, fieldName));
    }

    public static ParseException invalidDateFormat(String dateString) {
        return new ParseException(String.format(MSG_INVALID_DATE_FORMAT, dateString));
    }

    public static ParseException invalidAmountFormat(String amountString) {
        return new ParseException(String.format(MSG_INVALID_AMOUNT_FORMAT, amountString));
    }

    public static ParseException dateNotInExpectedMonth(int lineNumber) {
        return new ParseException(String.format(MSG_DATE_NOT_IN_EXPECTED_MONTH, lineNumber));
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
}