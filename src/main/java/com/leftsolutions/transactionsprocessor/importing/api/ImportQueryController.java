package com.leftsolutions.transactionsprocessor.importing.api;

import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import com.leftsolutions.transactionsprocessor.security.WorkspaceProvider;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imports")
class ImportQueryController {

    private final ImportingFacade importingFacade;
    private final WorkspaceProvider workspaceProvider;

    @GetMapping("/months/{month}/status")
    ImportJobStatusDto status(@PathVariable @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
        var workspaceId = workspaceProvider.currentWorkspaceId();
        return importingFacade.getStatus(workspaceId, YearMonth.parse(month));
    }
}
