package com.leftsolutions.transactionsprocessor.importing.domain;


import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;

import java.time.YearMonth;
import java.util.List;

public interface ImportingFacade {

    void markProcessing(String workspaceId, YearMonth month);

    void markCompleted(String workspaceId, YearMonth month, int importedRows, int rejectedRows, List<String> errors);

    void markFailed(String workspaceId, YearMonth month, String error);

    ImportJobStatusDto getStatus(String workspaceId, YearMonth month);

    boolean isCompleted(String workspaceId, YearMonth month);
}
