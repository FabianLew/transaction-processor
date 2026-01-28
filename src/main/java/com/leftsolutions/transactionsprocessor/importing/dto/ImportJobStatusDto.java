package com.leftsolutions.transactionsprocessor.importing.dto;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

public record ImportJobStatusDto(
        String workspaceId,
        YearMonth month,
        ImportJobState state,
        int importedRows,
        int rejectedRows,
        List<String> errors,
        Instant updatedAt
) {

    public static ImportJobStatusDto notFound(String workspaceId, YearMonth month) {
        return new ImportJobStatusDto(
                workspaceId,
                month,
                ImportJobState.NOT_FOUND,
                0,
                0,
                List.of(),
                Instant.now()
        );
    }
}
