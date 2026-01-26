package com.leftsolutions.transactionsprocessor.transaction.domain;

import java.io.InputStream;
import java.time.YearMonth;

public interface TransactionImportFacade {
    void importMonthly(String workspaceId, YearMonth month, InputStream csvInputStream);
}
