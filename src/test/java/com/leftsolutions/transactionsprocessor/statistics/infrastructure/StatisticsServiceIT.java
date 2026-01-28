package com.leftsolutions.transactionsprocessor.statistics.infrastructure;

import com.leftsolutions.transactionsprocessor.IntegrationTestConfig;
import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.statistics.domain.StatisticsFacade;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsResponseDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.MonthlyStatisticsRowDto;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsGroupBy;
import com.leftsolutions.transactionsprocessor.statistics.dto.StatisticsQuery;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsServiceIT extends IntegrationTestConfig {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth MONTH = YearMonth.of(2026, 1);

    @Autowired
    private TransactionImportFacade transactionImportFacade;

    @Autowired
    private ImportingFacade importingFacade;

    @Autowired
    private StatisticsFacade statisticsFacade;

    @Test
    void shouldReturnStatisticsGroupedByCategory() throws Exception {
        // given
        importSampleDataAndWait();

        var query = new StatisticsQuery(MONTH, StatisticsGroupBy.CATEGORY);

        // when
        var response = statisticsFacade.getMonthlyStatistics(WORKSPACE_ID, query);

        // then
        assertThat(response)
                .returns(WORKSPACE_ID, MonthlyStatisticsResponseDto::workspaceId)
                .returns(MONTH, MonthlyStatisticsResponseDto::month)
                .returns(StatisticsGroupBy.CATEGORY, MonthlyStatisticsResponseDto::groupedBy);

        assertThat(response.rows())
                .extracting(MonthlyStatisticsRowDto::key)
                .containsExactlyInAnyOrder("RENT", "FOOD");

        assertThat(response.rows())
                .filteredOn(r -> r.key().equals("FOOD"))
                .singleElement()
                .returns(2L, MonthlyStatisticsRowDto::transactionsCount);

        assertThat(response.rows())
                .filteredOn(r -> r.key().equals("RENT"))
                .singleElement()
                .returns(1L, MonthlyStatisticsRowDto::transactionsCount);
    }

    @Test
    void shouldReturnStatisticsGroupedByIban() throws Exception {
        // given
        importSampleDataAndWait();

        var query = new StatisticsQuery(MONTH, StatisticsGroupBy.IBAN);

        // when
        var response = statisticsFacade.getMonthlyStatistics(WORKSPACE_ID, query);

        // then
        assertThat(response.rows())
                .extracting(MonthlyStatisticsRowDto::key)
                .containsExactlyInAnyOrder(
                        "PL12109010140000071219812875",
                        "PL61109010140000071219812874"
                );
    }

    @Test
    void shouldReturnSummaryStatistics() throws Exception {
        // given
        importSampleDataAndWait();

        var query = new StatisticsQuery(MONTH, StatisticsGroupBy.SUMMARY);

        // when
        var response = statisticsFacade.getMonthlyStatistics(WORKSPACE_ID, query);

        // then
        assertThat(response.rows()).hasSize(1);

        assertThat(response.rows().getFirst())
                .returns("SUMMARY", MonthlyStatisticsRowDto::key)
                .returns(3L, MonthlyStatisticsRowDto::transactionsCount);
    }

    private void importSampleDataAndWait() throws Exception {
        // given
        var csv = """
                iban,date,currency,category,amount
                PL61109010140000071219812874,2026-01-10,PLN,FOOD,-10.50
                PL61109010140000071219812874,2026-01-11,PLN,FOOD,-20.00
                PL12109010140000071219812875,2026-01-12,PLN,RENT,-2000.00
                """;

        var csvFile = writeTempCsv(csv);

        // when
        transactionImportFacade.importMonthlyAsync(WORKSPACE_ID, MONTH, csvFile);

        // then
        assertThat(importingFacade.isCompleted(WORKSPACE_ID, MONTH)).isTrue();
    }


    private Path writeTempCsv(String csv) throws Exception {
        var file = Files.createTempFile("it-import-", ".csv");
        Files.writeString(file, csv, StandardCharsets.UTF_8);
        return file;
    }
}

