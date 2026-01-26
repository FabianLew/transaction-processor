package com.leftsolutions.transactionsprocessor.importing.api;

import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import jakarta.validation.constraints.Pattern;
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

    @PostMapping(value = "/workspaces/{workspaceId}/months/{month}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void importMonth(
            @PathVariable String workspaceId,
            @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String month,
            @RequestPart("file") MultipartFile file) throws Exception {
        transactionImportFacade.importMonthly(workspaceId, YearMonth.parse(month), file.getInputStream());
    }
}
