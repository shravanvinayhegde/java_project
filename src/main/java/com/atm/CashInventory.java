package com.atm;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** In-memory note inventory. Updates are validated before any notes are removed. */
public class CashInventory {
    private final EnumMap<Denomination, Integer> notes = new EnumMap<>(Denomination.class);

    public CashInventory(Map<Denomination, Integer> initialNotes) {
        for (Denomination denomination : Denomination.values()) {
            int count = initialNotes.getOrDefault(denomination, 0);
            if (count < 0) {
                throw new IllegalArgumentException("Note counts cannot be negative.");
            }
            notes.put(denomination, count);
        }
    }

    public synchronized int totalCash() {
        int total = 0;
        for (Map.Entry<Denomination, Integer> entry : notes.entrySet()) {
            total += entry.getKey().getValue() * entry.getValue();
        }
        return total;
    }

    public synchronized Map<Denomination, Integer> snapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(notes));
    }

    public synchronized void remove(Map<Denomination, Integer> dispensed)
            throws InsufficientCashException {
        // Validate the whole operation first, ensuring it is all-or-nothing.
        for (Map.Entry<Denomination, Integer> entry : dispensed.entrySet()) {
            Denomination denomination = entry.getKey();
            Integer count = entry.getValue();
            if (denomination == null || count == null || count < 0) {
                throw new InsufficientCashException("Invalid note breakdown.");
            }
            if (notes.getOrDefault(denomination, 0) < count) {
                throw new InsufficientCashException("Cassette for " + denomination + " has insufficient notes.");
            }
        }
        for (Map.Entry<Denomination, Integer> entry : dispensed.entrySet()) {
            notes.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }
    }
}
