package com.leftsolutions.transactionsprocessor.statistics.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.YearMonth;

@ResponseStatus(HttpStatus.CONFLICT)
public class StatisticsNotReadyException extends RuntimeException {
    private static final String MSG_NOT_READY = "Statistics not ready. Import not completed for workspaceId=%s, month=%s";

    public StatisticsNotReadyException(String workspaceId, YearMonth month) {
        super(String.format(MSG_NOT_READY, workspaceId, month));
    }
}
