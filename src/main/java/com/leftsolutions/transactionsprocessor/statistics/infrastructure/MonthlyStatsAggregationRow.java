package com.leftsolutions.transactionsprocessor.statistics.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
class MonthlyStatsAggregationRow {
    private String key;
    private long transactionsCount;
    private BigDecimal totalAmount;
}
