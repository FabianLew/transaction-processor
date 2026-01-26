package com.leftsolutions.transactionsprocessor.statistics.domain;

import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsResponseDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsQuery;

public interface StatisticsFacade {

    MonthlyStatisticsResponseDto getMonthlyStatistics(String workspaceId, StatisticsQuery query);
}
