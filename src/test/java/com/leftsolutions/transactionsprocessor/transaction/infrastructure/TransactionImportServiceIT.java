package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import com.leftsolutions.transactionsprocessor.IntegrationTestConfig;
import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionImportServiceIT extends IntegrationTestConfig {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth MONTH = YearMonth.of(2026, 1);

    @Autowired
    private TransactionImportFacade transactionImportFacade;

    @Autowired
    private ImportingFacade importingFacade;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanup() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldImportTransactionsAndMarkImportAsCompleted() {
        // given
        var csv = """
                iban,date,currency,category,amount
                PL61109010140000071219812874,2026-01-10,PLN,FOOD,-10.50
                PL61109010140000071219812874,2026-01-11,PLN,FOOD,-20.00
                PL12109010140000071219812875,2026-01-12,PLN,RENT,-2000.00
                """;

        // when
        transactionImportFacade.importMonthly(WORKSPACE_ID, MONTH, inputStream(csv));

        // then
        var status = importingFacade.getStatus(WORKSPACE_ID, MONTH);
        assertThat(status)
                .returns(WORKSPACE_ID, ImportJobStatusDto::workspaceId)
                .returns(MONTH, ImportJobStatusDto::month)
                .returns(ImportJobState.COMPLETED, ImportJobStatusDto::state)
                .returns(3, ImportJobStatusDto::importedRows)
                .returns(0, ImportJobStatusDto::rejectedRows);

        assertThat(transactionRepository.findAll())
                .hasSize(3);
    }

    @Test
    void shouldCountRejectedRowsAndStillCompleteImport() {
        // given
        var csv = """
                iban,date,currency,category,amount
                PL61109010140000071219812874,2026-01-10,PLN,FOOD,-10.50
                INVALID,2026-01-11,PLN,FOOD,-20.00
                PL12109010140000071219812875,2026-01-12,PLN,RENT,not-a-number
                PL12109010140000071219812875,2026-01-05,PLN,RENT,-100.00
                """;

        // when
        transactionImportFacade.importMonthly(WORKSPACE_ID, MONTH, inputStream(csv));

        // then
        var status = importingFacade.getStatus(WORKSPACE_ID, MONTH);
        assertThat(status)
                .returns(ImportJobState.COMPLETED, ImportJobStatusDto::state)
                .returns(2, ImportJobStatusDto::importedRows)
                .returns(2, ImportJobStatusDto::rejectedRows);

        assertThat(transactionRepository.findAll())
                .hasSize(2);
    }

    private ByteArrayInputStream inputStream(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}
