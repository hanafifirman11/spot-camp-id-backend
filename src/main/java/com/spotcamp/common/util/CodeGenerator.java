package com.spotcamp.common.util;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.Predicate;

public final class CodeGenerator {

    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 20;

    private CodeGenerator() {
    }

    public static String generate(String prefix, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        String safePrefix = Objects.requireNonNull(prefix, "prefix must not be null").trim();
        if (safePrefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix must not be empty");
        }
        StringBuilder builder = new StringBuilder(safePrefix.length() + 1 + length);
        builder.append(safePrefix).append('-');
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHANUM.length());
            builder.append(ALPHANUM.charAt(index));
        }
        return builder.toString();
    }

    public static String generateUnique(String prefix, int length, Predicate<String> isTaken) {
        Objects.requireNonNull(isTaken, "isTaken must not be null");
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = generate(prefix, length);
            if (!isTaken.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique code");
    }
}
