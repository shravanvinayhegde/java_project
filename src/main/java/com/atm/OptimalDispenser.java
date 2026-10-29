package com.atm;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/** Bounded dynamic programming strategy that minimises the total number of notes. */
public class OptimalDispenser implements DispenseStrategy {
    @Override
    public Map<Denomination, Integer> compute(
            int amount,
            Map<Denomination, Integer> available
    ) throws InsufficientCashException {
        if (amount < 0 || amount % 10 != 0) {
            throw new InsufficientCashException("Amount must be a non-negative multiple of Rs.10.");
        }

        int target = amount / 10;
        Denomination[] denominations = Denomination.values();
        int denominationCount = denominations.length;
        final int infinity = Integer.MAX_VALUE / 4;

        // best[i][sum] = minimum notes needed for 'sum' using the first i denominations.
        int[][] best = new int[denominationCount + 1][target + 1];
        for (int[] row : best) {
            Arrays.fill(row, infinity);
        }
        best[0][0] = 0;

        for (int i = 1; i <= denominationCount; i++) {
            Denomination denomination = denominations[i - 1];
            int unitValue = denomination.getValue() / 10;
            int maxCount = available.getOrDefault(denomination, 0);
            if (maxCount < 0) {
                throw new IllegalArgumentException("Available note counts cannot be negative.");
            }

            for (int sum = 0; sum <= target; sum++) {
                for (int count = 0; count <= maxCount && count * unitValue <= sum; count++) {
                    int previous = best[i - 1][sum - count * unitValue];
                    if (previous < infinity) {
                        best[i][sum] = Math.min(best[i][sum], previous + count);
                    }
                }
            }
        }

        if (best[denominationCount][target] >= infinity) {
            throw new InsufficientCashException(
                    "Optimal strategy cannot make Rs." + amount + " with the available notes."
            );
        }

        // Reconstruct a combination that achieves the minimum number of notes.
        Map<Denomination, Integer> result = new EnumMap<>(Denomination.class);
        int remaining = target;
        for (int i = denominationCount; i >= 1; i--) {
            Denomination denomination = denominations[i - 1];
            int unitValue = denomination.getValue() / 10;
            int maxCount = available.getOrDefault(denomination, 0);

            for (int count = 0; count <= maxCount && count * unitValue <= remaining; count++) {
                int previous = best[i - 1][remaining - count * unitValue];
                if (previous < infinity && previous + count == best[i][remaining]) {
                    if (count > 0) {
                        result.put(denomination, count);
                    }
                    remaining -= count * unitValue;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String name() {
        return "Optimal (dynamic programming)";
    }
}
