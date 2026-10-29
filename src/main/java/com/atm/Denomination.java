package com.atm;

/** Supported currency notes, ordered from highest to lowest value. */
public enum Denomination {
    N500(500),
    N200(200),
    N100(100),
    N50(50),
    N20(20),
    N10(10);

    private final int value;

    Denomination(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Rs." + value;
    }
}
