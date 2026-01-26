package com.leftsolutions.transactionsprocessor.transaction.infrastructure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;


interface TransactionRepository extends MongoRepository<TransactionDocument, UUID> {
    void deleteByWorkspaceIdAndYearAndMonth(String workspaceId, int year, int month);
}
