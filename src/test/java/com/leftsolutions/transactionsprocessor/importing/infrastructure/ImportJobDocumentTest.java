package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImportJobDocumentTest {

    private static final String WORKSPACE_ID = "workspace-1";
    private static final YearMonth MONTH = YearMonth.of(2026, 1);

    private static final UUID JOB_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final int IMPORTED_ROWS = 10;
    private static final int REJECTED_ROWS = 2;

    private static final String ERROR_MESSAGE = "Something failed";
    private static final Instant UPDATED_AT = Instant.parse("2026-01-10T10:00:00Z");

    @Test
    void shouldCreateProcessingJobWithDefaults() {
        // given // when
        var doc = ImportJobDocument.newProcessing(WORKSPACE_ID, MONTH);

        // then
        assertThat(doc)
                .returns(WORKSPACE_ID, ImportJobDocument::getWorkspaceId)
                .returns(MONTH.getYear(), ImportJobDocument::getYear)
                .returns(MONTH.getMonthValue(), ImportJobDocument::getMonth)
                .returns(ImportJobState.PROCESSING, ImportJobDocument::getState)
                .returns(0, ImportJobDocument::getImportedRows)
                .returns(0, ImportJobDocument::getRejectedRows)
                .returns(List.of(), ImportJobDocument::getErrors);

        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldMarkJobAsCompletedWhenRejectedRowsIsZero() {
        // given
        var base = baseProcessingJob();
        var importedRows = 10;
        var rejectedRows = 0;

        // when
        var updated = base.markCompleted(importedRows, rejectedRows, List.of());

        // then
        assertThat(updated)
                .returns(JOB_ID, ImportJobDocument::getId)
                .returns(WORKSPACE_ID, ImportJobDocument::getWorkspaceId)
                .returns(MONTH.getYear(), ImportJobDocument::getYear)
                .returns(MONTH.getMonthValue(), ImportJobDocument::getMonth)
                .returns(ImportJobState.COMPLETED, ImportJobDocument::getState)
                .returns(importedRows, ImportJobDocument::getImportedRows)
                .returns(rejectedRows, ImportJobDocument::getRejectedRows)
                .returns(List.of(), ImportJobDocument::getErrors);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(UPDATED_AT);
    }

    @Test
    void shouldMarkJobAsWithWarningWhenRejectedRowsIsGreaterThanZero() {
        // given
        var base = baseProcessingJob();
        var errors = List.of("Line 2: invalid iban", "Line 5: missing field amount");

        // when
        var updated = base.markCompleted(IMPORTED_ROWS, REJECTED_ROWS, errors);

        // then
        assertThat(updated)
                .returns(JOB_ID, ImportJobDocument::getId)
                .returns(WORKSPACE_ID, ImportJobDocument::getWorkspaceId)
                .returns(MONTH.getYear(), ImportJobDocument::getYear)
                .returns(MONTH.getMonthValue(), ImportJobDocument::getMonth)
                .returns(ImportJobState.WITH_WARNING, ImportJobDocument::getState)
                .returns(IMPORTED_ROWS, ImportJobDocument::getImportedRows)
                .returns(REJECTED_ROWS, ImportJobDocument::getRejectedRows)
                .returns(errors, ImportJobDocument::getErrors);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(UPDATED_AT);
    }

    @Test
    void shouldMarkJobAsFailedAndPreserveCounters() {
        // given
        var base = baseProcessingJobWithCounters();

        // when
        var updated = base.markFailed(ERROR_MESSAGE);

        // then
        assertThat(updated)
                .returns(JOB_ID, ImportJobDocument::getId)
                .returns(WORKSPACE_ID, ImportJobDocument::getWorkspaceId)
                .returns(MONTH.getYear(), ImportJobDocument::getYear)
                .returns(MONTH.getMonthValue(), ImportJobDocument::getMonth)
                .returns(ImportJobState.FAILED, ImportJobDocument::getState)
                .returns(IMPORTED_ROWS, ImportJobDocument::getImportedRows)
                .returns(REJECTED_ROWS, ImportJobDocument::getRejectedRows)
                .returns(List.of(ERROR_MESSAGE), ImportJobDocument::getErrors);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(UPDATED_AT);
    }

    @Test
    void shouldTreatCompletedAndWithWarningAsCompleted() {
        // given
        var completed = baseProcessingJob().markCompleted(IMPORTED_ROWS, 0, List.of());
        var withWarning = baseProcessingJob().markCompleted(IMPORTED_ROWS, 1, List.of("Line 2: invalid record"));

        // then
        assertThat(completed.isCompleted()).isTrue();
        assertThat(withWarning.isCompleted()).isTrue();
    }

    private static ImportJobDocument baseProcessingJob() {
        return ImportJobDocument.builder()
                .id(JOB_ID)
                .workspaceId(WORKSPACE_ID)
                .year(MONTH.getYear())
                .month(MONTH.getMonthValue())
                .state(ImportJobState.PROCESSING)
                .importedRows(0)
                .rejectedRows(0)
                .errors(List.of())
                .updatedAt(UPDATED_AT)
                .build();
    }

    private static ImportJobDocument baseProcessingJobWithCounters() {
        return ImportJobDocument.builder()
                .id(JOB_ID)
                .workspaceId(WORKSPACE_ID)
                .year(MONTH.getYear())
                .month(MONTH.getMonthValue())
                .state(ImportJobState.PROCESSING)
                .importedRows(IMPORTED_ROWS)
                .rejectedRows(REJECTED_ROWS)
                .errors(List.of())
                .updatedAt(UPDATED_AT)
                .build();
    }
}
