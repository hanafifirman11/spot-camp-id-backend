package com.spotcamp.authuser.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for authentication operations
 * Matches the AuthResponse schema in OpenAPI specification
 */
@Data
@Builder
public class AuthResponse {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("expiresIn")
    private long expiresIn;

    @JsonProperty("user")
    private UserInfoDto user;

    /**
     * Create successful auth response
     */
    public static AuthResponse success(String accessToken, long expiresIn, UserInfoDto user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}