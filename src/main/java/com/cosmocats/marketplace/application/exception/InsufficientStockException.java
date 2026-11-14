package com.cosmocats.marketplace.application.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID productId, int available, int requested) {
        super("Not enough stock for product " + productId
                + ": available " + available + ", requested " + requested + ".");
    }
}
