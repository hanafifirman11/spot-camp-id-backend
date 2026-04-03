package com.spotcamp.authuser.api.dto;

import com.spotcamp.authuser.domain.UserRole;
import lombok.Data;

@Data
public class CreateMerchantUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
}
