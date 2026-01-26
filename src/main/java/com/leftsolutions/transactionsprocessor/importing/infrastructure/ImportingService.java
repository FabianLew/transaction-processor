package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
class ImportingService implements ImportingFacade {

    private final ImportJobRepository importJobRepository;
    private final ImportJobMapper importJobMapper;

    @Override
    public void markProcessing(String workspaceId, YearMonth month) {
        var job = importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .map(ImportJobDocument::markProcessing)
                .orElseGet(() -> ImportJobDocument.newProcessing(workspaceId, month));

        importJobRepository.save(job);
    }

    @Override
    public void markCompleted(String workspaceId, YearMonth month, int importedRows, int rejectedRows) {
        var job = importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .orElseGet(() -> ImportJobDocument.newProcessing(workspaceId, month))
                .markCompleted(importedRows, rejectedRows);

        importJobRepository.save(job);
    }

    @Override
    public void markFailed(String workspaceId, YearMonth month, String error) {
        var job = importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .orElseGet(() -> ImportJobDocument.newProcessing(workspaceId, month))
                .markFailed(error);

        importJobRepository.save(job);
    }

    @Override
    public ImportJobStatusDto getStatus(String workspaceId, YearMonth month) {
        return importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .map(importJobMapper::toDto)
                .orElseGet(() -> new ImportJobStatusDto(
                        workspaceId,
                        month,
                        ImportJobState.NOT_FOUND,
                        0,
                        0,
                        null,
                        Instant.EPOCH
                ));
    }

    @Override
    public boolean isCompleted(String workspaceId, YearMonth month) {
        return importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .map(ImportJobDocument::isCompleted)
                .orElse(false);
    }
}
