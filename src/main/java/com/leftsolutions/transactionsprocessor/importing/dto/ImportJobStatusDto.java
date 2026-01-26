package com.leftsolutions.transactionsprocessor.importing.dto;

import java.time.Instant;
import java.time.YearMonth;

public record ImportJobStatusDto(
        String workspaceId,
        YearMonth month,
        ImportJobState state,
        int importedRows,
        int rejectedRows,
        String error,
        Instant updatedAt
) { }
