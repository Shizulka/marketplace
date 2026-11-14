package com.cosmocats.marketplace.application.exception;


public class CurrencyNotSupportedException extends RuntimeException {
    public CurrencyNotSupportedException(String currency) {
        super("Currency " + currency + " is not supported.");
    }
}