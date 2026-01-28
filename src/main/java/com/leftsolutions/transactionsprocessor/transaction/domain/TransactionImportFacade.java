package com.leftsolutions.transactionsprocessor.transaction.domain;

import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;

import java.nio.file.Path;
import java.time.YearMonth;

public interface TransactionImportFacade {
    ImportJobStatusDto importMonthlyAsync(String workspaceId, YearMonth month, Path csvFile);
}
