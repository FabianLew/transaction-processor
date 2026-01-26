package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface ImportJobRepository extends MongoRepository<ImportJobDocument, String> {

    Optional<ImportJobDocument> findByWorkspaceIdAndYearAndMonth(String workspaceId, int year, int month);
}
