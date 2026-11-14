package com.cosmocats.marketplace.application.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("Order with id " + id + " was not found.");
    }

    public OrderNotFoundException(String orderNumber) {
        super("Order with number " + orderNumber + " was not found.");
    }
}
