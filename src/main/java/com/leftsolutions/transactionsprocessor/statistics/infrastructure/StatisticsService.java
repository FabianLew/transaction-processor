package com.leftsolutions.transactionsprocessor.statistics.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.statistics.domain.StatisticsFacade;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsResponseDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsQuery;
import com.leftsolutions.transactionsprocessor.statistics.exception.StatisticsNotReadyException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
@RequiredArgsConstructor
class StatisticsService implements StatisticsFacade {

    private static final String TRANSACTIONS_COLLECTION = "transactions";
    private static final String FIELD_WORKSPACE_ID = "workspaceId";
    private static final String FIELD_YEAR = "year";
    private static final String FIELD_MONTH = "month";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_IBAN = "iban";

    private static final String AGG_SUMMARY_KEY = "SUMMARY";
    private static final String AGG_TRANSACTIONS_COUNT = "transactionsCount";
    private static final String AGG_TOTAL_AMOUNT = "totalAmount";
    private static final String AGG_KEY = "key";
    private static final String AMOUNT = "amount";

    private final ImportingFacade importingFacade;
    private final MongoTemplate mongoTemplate;
    private final StatisticsMapper mapper;

    @Override
    public MonthlyStatisticsResponseDto getMonthlyStatistics(String workspaceId, StatisticsQuery query) {
        var month = query.yearMonth();
        ensureReady(workspaceId, month);

        var rows = switch (query.groupBy()) {
            case CATEGORY -> aggregateGrouped(workspaceId, month, FIELD_CATEGORY);
            case IBAN -> aggregateGrouped(workspaceId, month, FIELD_IBAN);
            case SUMMARY -> aggregateSummary(workspaceId, month);
            case null -> aggregateSummary(workspaceId, month);
        };


        return new MonthlyStatisticsResponseDto(
                workspaceId,
                month,
                query.groupBy(),
                rows.stream().map(mapper::toDto).toList()
        );
    }

    private void ensureReady(String workspaceId, YearMonth month) {
        if (!importingFacade.isCompleted(workspaceId, month)) {
            throw new StatisticsNotReadyException(workspaceId, month);
        }
    }

    private List<MonthlyStatsAggregationRow> aggregateGrouped(String workspaceId, YearMonth month, String groupField) {
        var matchOp = matchForMonth(workspaceId, month);

        var groupOp = group("$" + groupField)
                .count().as(AGG_TRANSACTIONS_COUNT)
                .sum(AMOUNT).as(AGG_TOTAL_AMOUNT);

        var projectOp = project()
                .and("_id").as(AGG_KEY)
                .and(AGG_TRANSACTIONS_COUNT).as(AGG_TRANSACTIONS_COUNT)
                .and(AGG_TOTAL_AMOUNT).as(AGG_TOTAL_AMOUNT);

        var sortOp = sort(Sort.by(Sort.Direction.DESC, AGG_TOTAL_AMOUNT));

        var aggregation = newAggregation(matchOp, groupOp, projectOp, sortOp);

        return mongoTemplate.aggregate(aggregation, TRANSACTIONS_COLLECTION, MonthlyStatsAggregationRow.class)
                .getMappedResults();
    }

    private List<MonthlyStatsAggregationRow> aggregateSummary(String workspaceId, YearMonth month) {
        var matchOp = matchForMonth(workspaceId, month);

        var groupOp = group()
                .count().as(AGG_TRANSACTIONS_COUNT)
                .sum(AMOUNT).as(AGG_TOTAL_AMOUNT);

        var projectOp = project()
                .andExpression("'" + AGG_SUMMARY_KEY + "'").as(AGG_KEY)
                .and(AGG_TRANSACTIONS_COUNT).as(AGG_TRANSACTIONS_COUNT)
                .and(AGG_TOTAL_AMOUNT).as(AGG_TOTAL_AMOUNT);

        var aggregation = newAggregation(matchOp, groupOp, projectOp);

        return mongoTemplate.aggregate(aggregation, TRANSACTIONS_COLLECTION, MonthlyStatsAggregationRow.class)
                .getMappedResults();
    }

    private MatchOperation matchForMonth(String workspaceId, YearMonth month) {
        return match(
                org.springframework.data.mongodb.core.query.Criteria.where(FIELD_WORKSPACE_ID).is(workspaceId)
                        .and(FIELD_YEAR).is(month.getYear())
                        .and(FIELD_MONTH).is(month.getMonthValue())
        );
    }
}
