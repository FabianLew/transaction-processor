package com.leftsolutions.transactionsprocessor.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImportException extends RuntimeException {

    private static final String MSG_IMPORT_FAILED = "Import failed: %s";

    public ImportException(Throwable cause) {
        super(MSG_IMPORT_FAILED, cause);
    }
}