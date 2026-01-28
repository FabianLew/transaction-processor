package com.leftsolutions.transactionsprocessor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "import.async")
@Data
public class ImportAsyncProperties {

    /**
     * Core pool size for import executor
     */
    private int corePoolSize = 2;

    /**
     * Max pool size for import executor
     */
    private int maxPoolSize = 4;

    /**
     * Queue capacity for import executor
     */
    private int queueCapacity = 50;

    /**
     * Thread name prefix
     */
    private String threadNamePrefix = "import-";
}
