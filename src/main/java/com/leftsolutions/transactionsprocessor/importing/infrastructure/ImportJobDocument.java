package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import com.leftsolutions.transactionsprocessor.importing.dto.ImportJobState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document("import_jobs")
@CompoundIndex(
        name = "import_jobs_workspace_year_month_uq",
        def = "{'workspaceId': 1, 'year': 1, 'month': 1}",
        unique = true
)
class ImportJobDocument {

    @Id
    private UUID id;

    private String workspaceId;

    private int year;
    private int month;

    private ImportJobState state;

    private int importedRows;
    private int rejectedRows;

    private String error;
    private Instant updatedAt;

    static ImportJobDocument newProcessing(String workspaceId, YearMonth month) {
        return ImportJobDocument.builder()
                .id(UUID.randomUUID())
                .workspaceId(workspaceId)
                .year(month.getYear())
                .month(month.getMonthValue())
                .state(ImportJobState.PROCESSING)
                .importedRows(0)
                .rejectedRows(0)
                .error(null)
                .updatedAt(Instant.now())
                .build();
    }
    
    ImportJobDocument markProcessing() {
        return this.toBuilder()
                .state(ImportJobState.PROCESSING)
                .error(null)
                .updatedAt(Instant.now())
                .build();
    }

    ImportJobDocument markCompleted(int importedRows, int rejectedRows) {
        return this.toBuilder()
                .state(ImportJobState.COMPLETED)
                .importedRows(importedRows)
                .rejectedRows(rejectedRows)
                .error(null)
                .updatedAt(Instant.now())
                .build();
    }

    ImportJobDocument markFailed(String error) {
        return this.toBuilder()
                .state(ImportJobState.FAILED)
                .error(error)
                .updatedAt(Instant.now())
                .build();
    }

    boolean isCompleted() {
        return this.state == ImportJobState.COMPLETED;
    }
}

