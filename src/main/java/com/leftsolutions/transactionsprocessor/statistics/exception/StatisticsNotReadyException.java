package com.leftsolutions.transactionsprocessor.statistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;

public class StatisticsNotReadyException extends ResponseStatusException {
    private static final String MSG_NOT_READY = "Statistics not ready. Import not completed for workspaceId=%s, month=%s";

    public StatisticsNotReadyException(String workspaceId, YearMonth month) {
        super(HttpStatus.PRECONDITION_FAILED, String.format(MSG_NOT_READY, workspaceId, month));
    }
}
