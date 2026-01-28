package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import com.leftsolutions.transactionsprocessor.transaction.exception.ImportAlreadyProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;

@Service
@Slf4j
class TransactionImportService implements TransactionImportFacade {

    private static final int MAX_STORED_ERRORS = 200;

    private final ImportingFacade importingFacade;
    private final CsvTransactionParser csvTransactionParser;
    private final TransactionRepository transactionRepository;
    private final TransactionTemplate transactionTemplate;

    private final TaskExecutor importExecutor;

    public TransactionImportService(ImportingFacade importingFacade,
                                    CsvTransactionParser csvTransactionParser,
                                    TransactionRepository transactionRepository,
                                    TransactionTemplate transactionTemplate,
                                    @Qualifier("importExecutor") TaskExecutor importExecutor) {
        this.importingFacade = importingFacade;
        this.csvTransactionParser = csvTransactionParser;
        this.transactionRepository = transactionRepository;
        this.transactionTemplate = transactionTemplate;
        this.importExecutor = importExecutor;
    }

    @Override
    public ImportJobStatusDto importMonthlyAsync(String workspaceId, YearMonth month, Path csvFile) {
        var currentStatus = importingFacade.getStatus(workspaceId, month);
        if (currentStatus.state() == ImportJobState.PROCESSING) {
            throw new ImportAlreadyProcessingException(workspaceId, month);
        }

        importingFacade.markProcessing(workspaceId, month);

        importExecutor.execute(() -> runImport(workspaceId, month, csvFile));

        return importingFacade.getStatus(workspaceId, month);
    }

    private void runImport(String workspaceId, YearMonth month, Path csvFile) {
        try (InputStream input = Files.newInputStream(csvFile)) {
            var rows = csvTransactionParser.parse(workspaceId, input, month);

            var validDocs = rows.stream()
                    .filter(CsvTransactionParser.ParseResultRow::isValid)
                    .map(CsvTransactionParser.ParseResultRow::document)
                    .toList();

            var rejectedErrors = rows.stream()
                    .filter(r -> !r.isValid())
                    .map(CsvTransactionParser.ParseResultRow::error)
                    .map(this::normalizeErrorMessage)
                    .limit(MAX_STORED_ERRORS)
                    .toList();

            var rejectedRows = rows.size() - validDocs.size();

            transactionTemplate.executeWithoutResult(status -> {
                transactionRepository.deleteByWorkspaceIdAndYearAndMonth(
                        workspaceId,
                        month.getYear(),
                        month.getMonthValue()
                );

                transactionRepository.saveAll(validDocs);

                importingFacade.markCompleted(workspaceId, month, validDocs.size(), rejectedRows, rejectedErrors);
            });

            log.info("Import finished workspaceId={}, month={}, importedRows={}, rejectedRows={}",
                    workspaceId, month, validDocs.size(), rejectedRows);

        } catch (Exception e) {
            importingFacade.markFailed(workspaceId, month, e.getMessage());
            log.warn("Import failed workspaceId={}, month={}, error={}", workspaceId, month, e.getMessage(), e);
        } finally {
            try {
                Files.deleteIfExists(csvFile);
            } catch (Exception deleteError) {
                log.warn("Could not delete temp import file {}: {}", csvFile, deleteError.getMessage(), deleteError);
            }
        }
    }

    private String normalizeErrorMessage(String error) {
        if (error == null) {
            return null;
        }

        return error
                .replaceAll("^\\d+\\s+\\w+\\s+\"", "")
                .replaceAll("\"$", "")
                .replaceAll("[\\r\\n]", "")
                .trim();
    }
}
