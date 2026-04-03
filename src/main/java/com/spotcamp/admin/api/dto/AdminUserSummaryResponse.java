package com.spotcamp.admin.api.dto;

import com.spotcamp.authuser.domain.UserRole;
import com.spotcamp.authuser.domain.UserStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryResponse {
    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private UserStatus status;
    private String businessName;
    private String businessCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
