package com.leftsolutions.transactionsprocessor.statistics.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsResponseDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsRowDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsGroupBy;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsQuery;
import com.leftsolutions.transactionsprocessor.statistics.exception.StatisticsNotReadyException;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceGatingTest {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth MONTH = YearMonth.of(2026, 1);

    private static final MonthlyStatsAggregationRow SUMMARY_ROW =
            new MonthlyStatsAggregationRow("SUMMARY", 3L, new BigDecimal("-2030.50"));

    private static final MonthlyStatisticsRowDto SUMMARY_DTO =
            new MonthlyStatisticsRowDto("SUMMARY", 3L, new BigDecimal("-2030.50"));

    @Mock
    private ImportingFacade importingFacade;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private StatisticsMapper mapper;

    @Test
    void shouldThrowWhenImportIsNotCompleted() {
        // given
        var service = new StatisticsService(importingFacade, mongoTemplate, mapper);
        var query = new StatisticsQuery(MONTH, StatisticsGroupBy.SUMMARY);

        when(importingFacade.isCompleted(WORKSPACE_ID, MONTH)).thenReturn(false);

        // when // then
        assertThatThrownBy(() -> service.getMonthlyStatistics(WORKSPACE_ID, query))
                .isInstanceOf(StatisticsNotReadyException.class);

        verify(importingFacade).isCompleted(WORKSPACE_ID, MONTH);
        verifyNoInteractions(mongoTemplate);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldAggregateWhenImportIsCompleted() {
        // given
        var service = new StatisticsService(importingFacade, mongoTemplate, mapper);
        var query = new StatisticsQuery(MONTH, StatisticsGroupBy.SUMMARY);

        when(importingFacade.isCompleted(WORKSPACE_ID, MONTH)).thenReturn(true);

        var aggregationResults = new AggregationResults<>(
                List.of(SUMMARY_ROW),
                new Document()
        );

        when(mongoTemplate.aggregate(
                any(Aggregation.class),
                eq("transactions"),
                eq(MonthlyStatsAggregationRow.class)
        )).thenReturn(aggregationResults);

        when(mapper.toDto(SUMMARY_ROW)).thenReturn(SUMMARY_DTO);

        // when
        MonthlyStatisticsResponseDto response = service.getMonthlyStatistics(WORKSPACE_ID, query);

        // then
        assertThat(response)
                .returns(WORKSPACE_ID, MonthlyStatisticsResponseDto::workspaceId)
                .returns(MONTH, MonthlyStatisticsResponseDto::month)
                .returns(StatisticsGroupBy.SUMMARY, MonthlyStatisticsResponseDto::groupedBy);

        assertThat(response.rows())
                .hasSize(1);

        assertThat(response.rows().getFirst())
                .returns("SUMMARY", MonthlyStatisticsRowDto::key)
                .returns(3L, MonthlyStatisticsRowDto::transactionsCount)
                .returns(new BigDecimal("-2030.50"), MonthlyStatisticsRowDto::totalAmount);

        verify(importingFacade).isCompleted(WORKSPACE_ID, MONTH);
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("transactions"), eq(MonthlyStatsAggregationRow.class));
        verify(mapper).toDto(SUMMARY_ROW);
        verifyNoMoreInteractions(importingFacade, mongoTemplate, mapper);
    }
}
