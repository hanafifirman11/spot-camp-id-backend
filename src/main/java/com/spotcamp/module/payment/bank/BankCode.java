package com.spotcamp.module.payment.bank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum BankCode {
    BCA("BCA"),
    BRI("BRI"),
    BNI("BNI"),
    MANDIRI("Mandiri"),
    PERMATA("Permata"),
    CIMB("CIMB Niaga");

    private final String displayName;

    public static List<BankCode> list() {
        return Arrays.asList(values());
    }
}
