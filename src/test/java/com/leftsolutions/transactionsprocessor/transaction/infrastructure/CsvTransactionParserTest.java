package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CsvTransactionParserTest {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth EXPECTED_MONTH = YearMonth.of(2026, 1);

    private static final String HEADER = "iban,yearMonth,currency,category,amount";
    private static final String VALID_IBAN_1 = "PL61109010140000071219812874";
    private static final String VALID_IBAN_2 = "PL12109010140000071219812875";

    private static final String VALID_CURRENCY = "PLN";
    private static final String VALID_CATEGORY_FOOD = "FOOD";

    private static final String DATE_IN_MONTH_1 = "2026-01-10";
    private static final String DATE_IN_MONTH_2 = "2026-01-11";
    private static final String DATE_OUT_OF_MONTH = "2026-02-01";

    private static final String AMOUNT_NEGATIVE = "-10.50";
    private static final String AMOUNT_ZERO = "0";
    private static final String AMOUNT_INVALID = "not-a-number";

    private final CsvTransactionParser csvTransactionParser = new CsvTransactionParser();

    @Test
    void shouldParseValidRowsAsSuccess() throws Exception {
        // given
        var csv = String.join("\n",
                HEADER,
                row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE),
                row(VALID_IBAN_2, DATE_IN_MONTH_2, VALID_CURRENCY, "RENT", "-2000.00")
        );

        // when
        var results = csvTransactionParser.parse(WORKSPACE_ID, inputStream(csv), EXPECTED_MONTH);

        // then
        assertThat(results)
                .hasSize(2);

        assertThat(results.getFirst().error()).isNull();
        assertThat(results.getFirst().document())
                .returns(WORKSPACE_ID, TransactionDocument::getWorkspaceId)
                .returns(EXPECTED_MONTH.getYear(), TransactionDocument::getYear)
                .returns(EXPECTED_MONTH.getMonthValue(), TransactionDocument::getMonth)
                .returns(VALID_IBAN_1, TransactionDocument::getIban)
                .returns(LocalDate.parse(DATE_IN_MONTH_1), TransactionDocument::getTransactionDate)
                .returns(VALID_CURRENCY, TransactionDocument::getCurrency)
                .returns(VALID_CATEGORY_FOOD, TransactionDocument::getCategory)
                .returns(new BigDecimal(AMOUNT_NEGATIVE), TransactionDocument::getAmount);

        assertThat(results.get(1).error()).isNull();
        assertThat(results.get(1).document())
                .returns(VALID_IBAN_2, TransactionDocument::getIban);
    }

    @ParameterizedTest
    @MethodSource("invalidRowCases")
    void shouldReturnFailureForInvalidRow(String csvRow, String expectedErrorContains) throws Exception {
        // given
        var csv = String.join("\n", HEADER, csvRow);

        // when
        var results = csvTransactionParser.parse(WORKSPACE_ID, inputStream(csv), EXPECTED_MONTH);

        // then
        assertThat(results)
                .hasSize(1);

        assertThat(results.getFirst().document()).isNull();
        assertThat(results.getFirst().error())
                .isNotBlank()
                .containsIgnoringCase(expectedErrorContains);
    }

    @Test
    void shouldReturnMixedResultsWhenSomeRowsAreInvalid() throws Exception {
        // given
        var csv = String.join("\n",
                HEADER,
                row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE),
                row("INVALID", DATE_IN_MONTH_2, VALID_CURRENCY, VALID_CATEGORY_FOOD, "-20.00"),
                row(VALID_IBAN_2, DATE_IN_MONTH_2, VALID_CURRENCY, "RENT", "-2000.00")
        );

        // when
        var results = csvTransactionParser.parse(WORKSPACE_ID, inputStream(csv), EXPECTED_MONTH);

        // then
        assertThat(results)
                .hasSize(3);

        var successDocuments = results.stream()
                .filter(row -> row.error() == null)
                .map(CsvTransactionParser.ParseResultRow::document)
                .toList();

        var errorMessages = results.stream()
                .map(CsvTransactionParser.ParseResultRow::error)
                .filter(Objects::nonNull)
                .toList();

        assertThat(successDocuments).hasSize(2);
        assertThat(errorMessages).hasSize(1);
        assertThat(errorMessages.getFirst()).containsIgnoringCase("iban");
    }

    private static Stream<Arguments> invalidRowCases() {
        return Stream.of(
                Arguments.of(row("", DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "iban"),
                Arguments.of(row(VALID_IBAN_1, "", VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "yearMonth"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, "", VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "currency"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, "", AMOUNT_NEGATIVE), "category"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, ""), "amount"),

                Arguments.of(row("INVALID", DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "iban"),
                Arguments.of(row(VALID_IBAN_1, "2026-01-XX", VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "yearMonth"),
                Arguments.of(row(VALID_IBAN_1, DATE_OUT_OF_MONTH, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "month"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, "PL", VALID_CATEGORY_FOOD, AMOUNT_NEGATIVE), "currency"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, tooLongCategory(), AMOUNT_NEGATIVE), "category"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_INVALID), "amount"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, VALID_CATEGORY_FOOD, AMOUNT_ZERO), "non"),
                Arguments.of(row(VALID_IBAN_1, DATE_IN_MONTH_1, VALID_CURRENCY, "   ", AMOUNT_NEGATIVE), "category")
        );
    }

    private static String row(String iban, String date, String currency, String category, String amount) {
        return String.join(",", iban, date, currency, category, amount);
    }

    private static String tooLongCategory() {
        return "X".repeat(101);
    }

    private static InputStream inputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
