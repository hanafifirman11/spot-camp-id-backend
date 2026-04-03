package com.spotcamp.module.booking.entity;

import java.util.Arrays;

public enum ManualTransferBank {
    BCA("BCA", "Bank Central Asia", "SpotCamp ID", "1234567890"),
    BRI("BRI", "Bank Rakyat Indonesia", "SpotCamp ID", "9876543210"),
    BNI("BNI", "Bank Negara Indonesia", "SpotCamp ID", "5566778899"),
    MANDIRI("MANDIRI", "Bank Mandiri", "SpotCamp ID", "1122334455"),
    PERMATA("PERMATA", "Permata Bank", "SpotCamp ID", "2233445566"),
    CIMB("CIMB", "CIMB Niaga", "SpotCamp ID", "3344556677");

    private final String code;
    private final String displayName;
    private final String accountName;
    private final String accountNumber;

    ManualTransferBank(String code, String displayName, String accountName, String accountNumber) {
        this.code = code;
        this.displayName = displayName;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public static ManualTransferBank fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(bank -> bank.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
