package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import com.leftsolutions.transactionsprocessor.transaction.exception.ImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class TransactionImportService implements TransactionImportFacade {
    private static final int MAX_STORED_ERRORS = 100;

    private final ImportingFacade importingFacade;
    private final CsvTransactionParser csvTransactionParser;
    private final TransactionRepository transactionRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void importMonthly(String workspaceId, YearMonth month, InputStream csvInputStream) {
        log.debug("Importing monthly transactions for workspaceId={}, month={}", workspaceId, month);
        importingFacade.markProcessing(workspaceId, month);

        var rows = parseRows(workspaceId, month, csvInputStream);

        var validDocs = rows.stream()
                .filter(CsvTransactionParser.ParseResultRow::isValid)
                .map(CsvTransactionParser.ParseResultRow::document)
                .toList();

        var rejectedErrors = rows.stream()
                .filter(row -> !row.isValid())
                .map(CsvTransactionParser.ParseResultRow::error)
                .limit(MAX_STORED_ERRORS)
                .toList();

        var rejected = rows.size() - validDocs.size();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                transactionRepository.deleteByWorkspaceIdAndYearAndMonth(
                        workspaceId,
                        month.getYear(),
                        month.getMonthValue()
                );

                transactionRepository.saveAll(validDocs);

                importingFacade.markCompleted(workspaceId, month, validDocs.size(), rejected, rejectedErrors);
            });

            log.debug("Imported transactions for workspaceId={}, month={}, importedRows={}, rejectedRows={}",
                    workspaceId, month, validDocs.size(), rejected);
        } catch (Exception e) {
            importingFacade.markFailed(workspaceId, month, e.getMessage());
            log.debug("Import failed for workspaceId={}, month={}: {}", workspaceId, month, e.getMessage());
            throw new ImportException(e);
        }
    }

    private List<CsvTransactionParser.ParseResultRow> parseRows(String workspaceId, YearMonth month, InputStream csvInputStream) {
        try {
            return csvTransactionParser.parse(workspaceId, csvInputStream, month);
        } catch (Exception e) {
            importingFacade.markFailed(workspaceId, month, e.getMessage());
            throw new ImportException(e);
        }
    }
}
