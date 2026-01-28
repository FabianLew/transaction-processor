package com.leftsolutions.transactionsprocessor.importing.infrastructure;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface ImportJobRepository extends MongoRepository<@NotNull ImportJobDocument, @NotNull String> {

    Optional<ImportJobDocument> findByWorkspaceIdAndYearAndMonth(String workspaceId, int year, int month);
}
