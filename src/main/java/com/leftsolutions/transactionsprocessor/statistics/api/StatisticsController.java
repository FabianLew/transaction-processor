package com.leftsolutions.transactionsprocessor.statistics.api;

import com.leftsolutions.transactionsprocessor.security.WorkspaceProvider;
import com.leftsolutions.transactionsprocessor.statistics.domain.StatisticsFacade;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsResponseDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
class StatisticsController {

    private final StatisticsFacade statisticsFacade;
    private final WorkspaceProvider workspaceProvider;

    @GetMapping
    MonthlyStatisticsResponseDto getMonthly(@Valid StatisticsQuery query) {
        var workspaceId = workspaceProvider.currentWorkspaceId();
        return statisticsFacade.getMonthlyStatistics(workspaceId, query);
    }
}
