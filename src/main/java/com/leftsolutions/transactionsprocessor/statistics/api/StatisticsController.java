package com.leftsolutions.transactionsprocessor.statistics.api;

import com.leftsolutions.transactionsprocessor.statistics.domain.StatisticsFacade;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsResponseDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
class StatisticsController {

    private final StatisticsFacade statisticsFacade;

    @GetMapping("/workspaces/{workspaceId}")
    MonthlyStatisticsResponseDto getMonthly(
            @PathVariable String workspaceId,
            @Valid StatisticsQuery query
    ) {
        return statisticsFacade.getMonthlyStatistics(workspaceId, query);
    }
}
