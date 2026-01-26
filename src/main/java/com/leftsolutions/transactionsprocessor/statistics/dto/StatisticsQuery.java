package com.leftsolutions.transactionsprocessor.statistics.dto;

import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record StatisticsQuery(
        @NotNull YearMonth date,
        @NotNull StatisticsGroupBy groupBy
) {
}
