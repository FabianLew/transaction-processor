package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import com.leftsolutions.transactionsprocessor.transaction.exception.ImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
class TransactionImportService implements TransactionImportFacade {

    private final ImportingFacade importingFacade;
    private final CsvTransactionParser csvTransactionParser;
    private final TransactionRepository transactionRepository;

    @Override
    public void importMonthly(String workspaceId, YearMonth month, InputStream csvInputStream) {
        log.debug("Importing monthly transactions for workspaceId={}, month={}", workspaceId, month);
        importingFacade.markProcessing(workspaceId, month);

        try {
            var rows = csvTransactionParser.parse(workspaceId, csvInputStream, month);

            transactionRepository.deleteByWorkspaceIdAndYearAndMonth(
                    workspaceId,
                    month.getYear(),
                    month.getMonthValue()
            );

            var validDocs = rows.stream()
                    .filter(row -> row.error() == null)
                    .map(CsvTransactionParser.ParseResultRow::document)
                    .toList();

            transactionRepository.saveAll(validDocs);

            var rejected = (int) rows.stream().filter(row -> row.error() != null).count();
            importingFacade.markCompleted(workspaceId, month, validDocs.size(), rejected);

            log.debug("Imported transactions for workspaceId={}, month={}", workspaceId, month);
        } catch (Exception e) {
            importingFacade.markFailed(workspaceId, month, e.getMessage());
            log.debug("Import failed for workspaceId={}, month={}: {}", workspaceId, month, e.getMessage());
            throw new ImportException(e);
        }
    }
}