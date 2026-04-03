package com.spotcamp.authuser.api.dto;

import com.spotcamp.authuser.domain.UserRole;
import lombok.Data;

@Data
public class UpdateMerchantUserRoleRequest {
    private UserRole role;
}
