package com.atm;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Coordinates validation, denomination selection, debiting and note removal. */
public class ATM {
    private final CashInventory inventory;
    private final ReentrantLock machineLock = new ReentrantLock(true);
    private volatile DispenseStrategy strategy;

    public ATM(CashInventory inventory, DispenseStrategy strategy) {
        if (inventory == null || strategy == null) {
            throw new IllegalArgumentException("Inventory and strategy are required.");
        }
        this.inventory = inventory;
        this.strategy = strategy;
    }

    public void setStrategy(DispenseStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null.");
        }
        machineLock.lock();
        try {
            this.strategy = strategy;
        } finally {
            machineLock.unlock();
        }
    }

    public String getStrategyName() {
        return strategy.name();
    }

    public int getCashAvailable() {
        return inventory.totalCash();
    }

    public Map<Denomination, Integer> withdraw(Account account, int amount) throws ATMException {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        machineLock.lock();
        try {
            validateAmount(amount);
            if (amount > inventory.totalCash()) {
                throw new InsufficientCashException("ATM has insufficient total cash.");
            }

            Map<Denomination, Integer> notes = strategy.compute(amount, inventory.snapshot());
            account.debit(amount);
            try {
                inventory.remove(notes);
            } catch (InsufficientCashException exception) {
                account.rollbackDebit(amount);
                throw exception;
            }

            account.record(new Transaction("WITHDRAW", amount, "SUCCESS"));
            return new EnumMap<>(notes);
        } catch (ATMException exception) {
            account.record(new Transaction("WITHDRAW", amount, "FAILED"));
            throw exception;
        } finally {
            machineLock.unlock();
        }
    }

    private void validateAmount(int amount) throws InvalidAmountException {
        if (amount < 100 || amount > 20_000 || amount % 10 != 0) {
            throw new InvalidAmountException(
                    "Amount must be Rs.100–Rs.20,000 and a multiple of Rs.10."
            );
        }
    }
}
