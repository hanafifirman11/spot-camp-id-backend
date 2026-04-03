package com.spotcamp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    
    private String secret;
    private long expiration = 3600000; // 1 hour
    private long refreshExpiration = 604800000; // 7 days
    private String issuer = "spot-camp-backend";
}