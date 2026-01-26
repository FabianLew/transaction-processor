package com.leftsolutions.transactionsprocessor.statistics.infrastructure;

import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsRowDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface StatisticsMapper {

    MonthlyStatisticsRowDto toDto(MonthlyStatsAggregationRow row);
}
