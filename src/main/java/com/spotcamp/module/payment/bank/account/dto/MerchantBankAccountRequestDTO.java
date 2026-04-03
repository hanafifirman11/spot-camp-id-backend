package com.spotcamp.module.payment.bank.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantBankAccountRequestDTO {
    @NotBlank
    private String bankCode;

    @NotBlank
    private String accountName;

    @NotBlank
    private String accountNumber;

    @NotNull
    private Boolean isActive;
}
