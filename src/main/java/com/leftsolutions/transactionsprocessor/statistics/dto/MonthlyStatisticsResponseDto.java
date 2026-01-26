package com.leftsolutions.transactionsprocessor.statistics.dto;

import java.time.YearMonth;
import java.util.List;

public record MonthlyStatisticsResponseDto(
        String workspaceId,
        YearMonth month,
        StatisticsGroupBy groupedBy,
        List<MonthlyStatisticsRowDto> rows
) {
}
