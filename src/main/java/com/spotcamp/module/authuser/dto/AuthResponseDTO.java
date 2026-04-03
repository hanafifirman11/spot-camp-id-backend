package com.spotcamp.module.authuser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for authentication operations
 * Matches the AuthResponseDTO schema in OpenAPI specification
 */
@Data
@Builder
public class AuthResponseDTO {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("tokenType")
    private String tokenType;

    @JsonProperty("expiresIn")
    private long expiresIn;

    @JsonProperty("user")
    private UserInfoDTO user;

    /**
     * Create successful auth response
     */
    public static AuthResponseDTO success(String accessToken, long expiresIn, UserInfoDTO user) {
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}