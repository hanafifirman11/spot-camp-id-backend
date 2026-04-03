package com.spotcamp.module.admin.dto;

import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryResponseDTO {
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
