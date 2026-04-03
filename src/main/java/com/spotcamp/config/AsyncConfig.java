package com.spotcamp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ConditionalOnProperty(value = "app.async.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncConfig {
}
