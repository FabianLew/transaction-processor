package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;


interface TransactionRepository extends MongoRepository<@NotNull TransactionDocument, @NotNull UUID> {
    void deleteByWorkspaceIdAndYearAndMonth(String workspaceId, int year, int month);
}
