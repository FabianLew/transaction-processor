package com.leftsolutions.transactionsprocessor.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;

public class ImportAlreadyProcessingException extends ResponseStatusException {

    private static final String MSG_IMPORT_ALREADY_PRECESSING = "Import already in progress for workspaceId=%s and month=%s";

    public ImportAlreadyProcessingException(String workspaceId, YearMonth month) {
        super(HttpStatus.CONFLICT, MSG_IMPORT_ALREADY_PRECESSING.formatted(workspaceId, month));
    }
}
