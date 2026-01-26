package com.leftsolutions.transactionsprocessor.statistics.dto;

import java.math.BigDecimal;

public record MonthlyStatisticsRowDto(
        String key,
        long transactionsCount,
        BigDecimal totalAmount
) {
}
