package com.atm;

import java.util.Map;

/** Pluggable algorithm for choosing the notes used for a withdrawal. */
public interface DispenseStrategy {
    Map<Denomination, Integer> compute(
            int amount,
            Map<Denomination, Integer> available
    ) throws InsufficientCashException;

    String name();
}
