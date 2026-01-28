package com.leftsolutions.transactionsprocessor.importing.api;

import com.leftsolutions.transactionsprocessor.IntegrationTestConfig;
import com.leftsolutions.transactionsprocessor.importing.domain.ImportingFacade;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobStatusDto;
import com.leftsolutions.transactionsprocessor.transaction.domain.TransactionImportFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.YearMonth;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImportApiSecurityIT extends IntegrationTestConfig {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth MONTH = YearMonth.of(2026, 1);
    private static final String STATUS_URL = "/api/imports/months/2026-01/status";
    private static final String CLAIM_WORKSPACE_ID = "workspace_id";
    private static final ImportJobState IMPORT_JOB_STATE = ImportJobState.COMPLETED;
    private static final int IMPORTED_ROWS = 10;
    private static final int REJECTED_ROWS = 2;

    @MockitoBean
    private TransactionImportFacade transactionImportFacade;

    @MockitoBean
    private ImportingFacade importingFacade;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldReturn401WhenNoJwtProvided() throws Exception {
        // when // then
        mockMvc.perform(get(STATUS_URL))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldReturnStatusWhenJwtWithWorkspaceClaimProvided() throws Exception {
        // given
        var dto = new ImportJobStatusDto(
                WORKSPACE_ID,
                MONTH,
                IMPORT_JOB_STATE,
                IMPORTED_ROWS,
                REJECTED_ROWS,
                null,
                Instant.parse("2026-01-10T10:00:00Z")
        );

        when(importingFacade.getStatus(WORKSPACE_ID, MONTH)).thenReturn(dto);

        // when // then
        mockMvc.perform(
                        get(STATUS_URL)
                                .with(jwt().jwt(j -> j.claim(CLAIM_WORKSPACE_ID, WORKSPACE_ID)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspaceId").value(WORKSPACE_ID))
                .andExpect(jsonPath("$.month").value("2026-01"))
                .andExpect(jsonPath("$.state").value(IMPORT_JOB_STATE.name()))
                .andExpect(jsonPath("$.importedRows").value(IMPORTED_ROWS))
                .andExpect(jsonPath("$.rejectedRows").value(REJECTED_ROWS));
    }
}