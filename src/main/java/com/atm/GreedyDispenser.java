package com.atm;

import java.util.EnumMap;
import java.util.Map;

/** Takes as many of each larger note as possible before considering smaller notes. */
public class GreedyDispenser implements DispenseStrategy {
    @Override
    public Map<Denomination, Integer> compute(
            int amount,
            Map<Denomination, Integer> available
    ) throws InsufficientCashException {
        if (amount < 0 || amount % 10 != 0) {
            throw new InsufficientCashException("Amount must be a non-negative multiple of Rs.10.");
        }

        Map<Denomination, Integer> result = new EnumMap<>(Denomination.class);
        int remaining = amount;

        for (Denomination denomination : Denomination.values()) {
            int stock = available.getOrDefault(denomination, 0);
            if (stock < 0) {
                throw new IllegalArgumentException("Available note counts cannot be negative.");
            }
            int use = Math.min(remaining / denomination.getValue(), stock);
            if (use > 0) {
                result.put(denomination, use);
                remaining -= use * denomination.getValue();
            }
        }

        if (remaining != 0) {
            throw new InsufficientCashException(
                    "Greedy strategy cannot dispense Rs." + amount
                            + " (Rs." + remaining + " remains unpaid)."
            );
        }
        return result;
    }

    @Override
    public String name() {
        return "Greedy";
    }
}
