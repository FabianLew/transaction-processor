package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("transactions")
@CompoundIndex(
        name = "transactions_workspace_year_month_idx",
        def = "{'workspaceId': 1, 'year': 1, 'month': 1}"
)
class TransactionDocument {

    @Id
    private UUID id;

    @Indexed
    private String workspaceId;

    int year;
    int month;

    @Indexed
    private String iban;

    LocalDate transactionDate;
    private String currency;
    private String category;
    private BigDecimal amount;
}
