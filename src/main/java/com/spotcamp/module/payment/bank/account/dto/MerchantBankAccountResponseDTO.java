package com.spotcamp.module.payment.bank.account.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantBankAccountResponseDTO {
    private Long id;
    private String bankCode;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private boolean isActive;
}
