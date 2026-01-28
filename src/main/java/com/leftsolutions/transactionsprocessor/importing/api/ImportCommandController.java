package com.leftsolutions.transactionsprocessor.importing.api;

import com.leftsolutions.transactionsprocessor.security.WorkspaceProvider;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imports")
class ImportCommandController {

    private final TransactionImportFacade transactionImportFacade;

    private final WorkspaceProvider workspaceProvider;

    @PostMapping(value = "/{yearMonth}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void importMonth(@PathVariable YearMonth yearMonth,
                     @RequestPart("file") MultipartFile file) throws Exception {
        var workspaceId = workspaceProvider.currentWorkspaceId();
        transactionImportFacade.importMonthly(workspaceId, yearMonth, file.getInputStream());
    }
}
