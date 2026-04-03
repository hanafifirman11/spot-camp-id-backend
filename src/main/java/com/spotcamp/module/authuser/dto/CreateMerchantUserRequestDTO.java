package com.spotcamp.module.authuser.dto;

import com.spotcamp.module.authuser.entity.UserRole;
import lombok.Data;

@Data
public class CreateMerchantUserRequestDTO {
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
}
