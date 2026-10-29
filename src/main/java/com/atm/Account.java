package com.atm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory customer account with synchronised state-changing operations. */
public class Account {
    private final String cardNumber;
    // Demonstration-only PIN storage. A real system must never store a PIN in plain text.
    private final int pin;
    private int balance;
    private final int dailyLimit;
    private int withdrawnToday;
    private int failedAttempts;
    private boolean locked;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String cardNumber, int pin, int balance, int dailyLimit) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("Card number cannot be blank.");
        }
        if (pin < 0 || balance < 0 || dailyLimit < 0) {
            throw new IllegalArgumentException("PIN, balance and daily limit must be non-negative.");
        }
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = balance;
        this.dailyLimit = dailyLimit;
    }

    public synchronized void authenticate(String enteredCardNumber, int enteredPin)
            throws AuthenticationException {
        if (locked) {
            throw new AuthenticationException("Card is blocked. Contact your bank.");
        }

        if (!cardNumber.equals(enteredCardNumber) || enteredPin != pin) {
            failedAttempts++;
            if (failedAttempts >= 3) {
                locked = true;
                throw new AuthenticationException("Wrong credentials three times. Card blocked.");
            }
            throw new AuthenticationException(
                    "Wrong card number or PIN. Attempts left: " + (3 - failedAttempts)
            );
        }
        failedAttempts = 0;
    }

    /** PIN-only variant matching the simplified flow described in the project report. */
    public synchronized void authenticate(int enteredPin) throws AuthenticationException {
        authenticate(cardNumber, enteredPin);
    }

    public synchronized void debit(int amount)
            throws InsufficientFundsException, DailyLimitExceededException {
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient balance. Available: Rs." + balance);
        }
        if (withdrawnToday + amount > dailyLimit) {
            throw new DailyLimitExceededException("Daily withdrawal limit of Rs." + dailyLimit + " exceeded.");
        }
        balance -= amount;
        withdrawnToday += amount;
    }

    /** Reverts a debit if cash removal fails, including the daily-limit counter. */
    public synchronized void rollbackDebit(int amount) {
        balance += amount;
        withdrawnToday = Math.max(0, withdrawnToday - amount);
    }

    public synchronized void record(Transaction transaction) {
        transactions.add(transaction);
    }

    public synchronized List<Transaction> miniStatement() {
        return Collections.unmodifiableList(new ArrayList<>(transactions));
    }

    public synchronized int getBalance() {
        return balance;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public synchronized int getWithdrawnToday() {
        return withdrawnToday;
    }

    public synchronized boolean isLocked() {
        return locked;
    }
}
