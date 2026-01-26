package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import com.leftsolutions.transactionsprocessor.transaction.exception.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
class CsvTransactionParser {

    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z0-9]{10,30}$");

    private static final String FIELD_IBAN = "iban";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_CURRENCY = "currency";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_AMOUNT = "amount";

    List<ParseResultRow> parse(String workspaceId, InputStream inputStream, YearMonth expectedMonth) throws IOException {
        try (var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            var records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            var results = new ArrayList<ParseResultRow>();

            var lineNumber = 2;
            for (var record : records) {
                results.add(parseRow(workspaceId, record, expectedMonth, lineNumber));
                lineNumber++;
            }

            return results;
        }
    }

    private ParseResultRow parseRow(String workspaceId, CSVRecord record, YearMonth expectedMonth, int lineNumber) {
        try {
            var iban = getRequiredField(record, FIELD_IBAN).trim().toUpperCase();
            var date = parseDate(getRequiredField(record, FIELD_DATE).trim());
            var currency = getRequiredField(record, FIELD_CURRENCY).trim().toUpperCase();
            var category = getRequiredField(record, FIELD_CATEGORY).trim();
            var amount = parseAmount(getRequiredField(record, FIELD_AMOUNT).trim());

            validateIban(iban, lineNumber);
            validateDateInMonth(date, expectedMonth, lineNumber);
            validateCurrency(currency, lineNumber);
            validateCategory(category, lineNumber);
            validateAmountNonZero(amount, lineNumber);

            var document = TransactionDocument.builder()
                    .id(UUID.randomUUID())
                    .workspaceId(workspaceId)
                    .year(expectedMonth.getYear())
                    .month(expectedMonth.getMonthValue())
                    .iban(iban)
                    .transactionDate(date)
                    .currency(currency)
                    .category(category)
                    .amount(amount)
                    .build();

            return ParseResultRow.success(document);
        } catch (ParseException e) {
            return ParseResultRow.failure(e.getMessage());
        }
    }

    private String getRequiredField(CSVRecord record, String fieldName) {
        var value = record.get(fieldName);
        if (value == null || value.isBlank()) {
            throw ParseException.missingField(record.getRecordNumber(), fieldName);
        }
        return value;
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw ParseException.invalidDateFormat(dateString);
        }
    }

    private BigDecimal parseAmount(String amountString) {
        try {
            return new BigDecimal(amountString);
        } catch (NumberFormatException e) {
            throw ParseException.invalidAmountFormat(amountString);
        }
    }

    private void validateDateInMonth(LocalDate date, YearMonth expectedMonth, int lineNumber) {
        if (!YearMonth.from(date).equals(expectedMonth)) {
            throw ParseException.dateNotInExpectedMonth(lineNumber);
        }
    }

    private void validateIban(String iban, int lineNumber) {
        if (!IBAN_PATTERN.matcher(iban).matches()) {
            throw ParseException.invalidIbanFormat(lineNumber);
        }
    }

    private void validateCurrency(String currency, int lineNumber) {
        if (!CURRENCY_PATTERN.matcher(currency).matches()) {
            throw ParseException.invalidCurrency(lineNumber);
        }
    }

    private void validateCategory(String category, int lineNumber) {
        if (category.isBlank() || category.length() > 100) {
            throw ParseException.invalidCategory(lineNumber);
        }
    }

    private void validateAmountNonZero(BigDecimal amount, int lineNumber) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw ParseException.amountMustBeNonZero(lineNumber);
        }
    }

    record ParseResultRow(TransactionDocument document, String error) {
        static ParseResultRow success(TransactionDocument document) {
            return new ParseResultRow(document, null);
        }

        static ParseResultRow failure(String error) {
            return new ParseResultRow(null, error);
        }
    }
}