package com.leftsolutions.transactionsprocessor.transaction.exception;

public class ImportException extends RuntimeException {

    private static final String MSG_IMPORT_FAILED = "Import failed: %s";

    public ImportException(Throwable cause) {
        super(MSG_IMPORT_FAILED, cause);
    }
}