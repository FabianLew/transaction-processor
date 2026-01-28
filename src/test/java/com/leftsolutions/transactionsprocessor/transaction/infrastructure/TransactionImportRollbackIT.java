package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import com.leftsolutions.transactionsprocessor.IntegrationTestConfig;
import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

class TransactionImportRollbackIT extends IntegrationTestConfig {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth MONTH = YearMonth.of(2026, 1);

    private static final String IBAN_1 = "PL61109010140000071219812874";
    private static final String IBAN_2 = "PL12109010140000071219812875";

    @Autowired
    private TransactionImportFacade transactionImportFacade;

    @Autowired
    private ImportingFacade importingFacade;

    @MockitoSpyBean
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanup() {
        reset(transactionRepository);
        transactionRepository.deleteAll();
    }

    @Test
    void shouldRollbackTransactionsWhenSavingFailsInsideTransaction() throws Exception {
        // given
        seedExistingTransactionsForMonth();

        doThrow(new RuntimeException("saveAll boom"))
                .when(transactionRepository)
                .saveAll(any(Iterable.class));

        var csv = """
                iban,date,currency,category,amount
                PL61109010140000071219812874,2026-01-10,PLN,FOOD,-10.50
                PL12109010140000071219812875,2026-01-11,PLN,RENT,-2000.00
                """;

        var csvFile = writeTempCsv(csv);

        // when
        transactionImportFacade.importMonthlyAsync(WORKSPACE_ID, MONTH, csvFile);
        var status = importingFacade.getStatus(WORKSPACE_ID, MONTH);

        // then
        assertThat(status)
                .returns(WORKSPACE_ID, ImportJobStatusDto::workspaceId)
                .returns(MONTH, ImportJobStatusDto::month)
                .returns(ImportJobState.FAILED, ImportJobStatusDto::state);

        assertThat(status.errors()).isNotEmpty();
        assertThat(transactionRepository.findAll()).hasSize(2);
    }


    private void seedExistingTransactionsForMonth() {
        transactionRepository.save(TransactionDocument.builder()
                .id(UUID.randomUUID())
                .workspaceId(WORKSPACE_ID)
                .year(MONTH.getYear())
                .month(MONTH.getMonthValue())
                .iban(IBAN_1)
                .transactionDate(LocalDate.of(2026, 1, 5))
                .currency("PLN")
                .category("FOOD")
                .amount(new java.math.BigDecimal("-5.00"))
                .build());

        transactionRepository.save(TransactionDocument.builder()
                .id(UUID.randomUUID())
                .workspaceId(WORKSPACE_ID)
                .year(MONTH.getYear())
                .month(MONTH.getMonthValue())
                .iban(IBAN_2)
                .transactionDate(LocalDate.of(2026, 1, 6))
                .currency("PLN")
                .category("RENT")
                .amount(new java.math.BigDecimal("-1000.00"))
                .build());
    }

    private Path writeTempCsv(String csv) throws Exception {
        var file = Files.createTempFile("it-import-", ".csv");
        Files.writeString(file, csv, StandardCharsets.UTF_8);
        return file;
    }

}
