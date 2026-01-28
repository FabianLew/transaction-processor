package com.leftsolutions.transactionsprocessor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
class TestAsyncOverrideConfig {

    @Primary
    @Bean(name = "importExecutor")
    TaskExecutor importExecutor() {
        return new SyncTaskExecutor();
    }
}
