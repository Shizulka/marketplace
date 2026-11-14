package com.cosmocats.marketplace.application.exception;

public class ExchangeRateUnavailableException extends RuntimeException {
    public ExchangeRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}