package com.spotcamp.module.admin.dto;

import com.spotcamp.module.authuser.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBusinessSummaryResponseDTO {
    private Long id;
    private String businessName;
    private String businessCode;
    private String ownerName;
    private String email;
    private String phone;
    private UserStatus status;
    private long totalCampsites;
    private long activeCampsites;
    private LocalDateTime createdAt;
}
