package com.spotcamp.admin.api.dto;

import com.spotcamp.authuser.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBusinessDetailResponse {
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
    private LocalDateTime updatedAt;
}
