package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

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
    public void markCompleted(String workspaceId, YearMonth month, int importedRows, int rejectedRows, List<String> errors) {
        var job = importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .orElseGet(() -> ImportJobDocument.newProcessing(workspaceId, month))
                .markCompleted(importedRows, rejectedRows, errors);

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
                .orElseGet(() -> ImportJobStatusDto.notFound(workspaceId, month));
    }

    @Override
    public boolean isCompleted(String workspaceId, YearMonth month) {
        return importJobRepository
                .findByWorkspaceIdAndYearAndMonth(workspaceId, month.getYear(), month.getMonthValue())
                .map(ImportJobDocument::isCompleted)
                .orElse(false);
    }
}
