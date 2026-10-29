package com.atm;

/** Base checked exception for ATM business-rule failures. */
public class ATMException extends Exception {
    public ATMException(String message) {
        super(message);
    }
}
