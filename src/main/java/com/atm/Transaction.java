package com.atm;

import java.time.LocalDateTime;

/** Immutable transaction entry used by the mini statement. */
public record Transaction(String type, int amount, String status, LocalDateTime timestamp) {
    public Transaction(String type, int amount, String status) {
        this(type, amount, status, LocalDateTime.now());
    }

    public Transaction {
        if (type == null || status == null || timestamp == null) {
            throw new IllegalArgumentException("Transaction fields cannot be null.");
        }
    }
}
