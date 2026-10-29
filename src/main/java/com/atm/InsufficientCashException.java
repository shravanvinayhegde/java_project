package com.atm;

public class InsufficientCashException extends ATMException {
    public InsufficientCashException(String message) {
        super(message);
    }
}
