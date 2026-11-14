package com.cosmocats.marketplace.application.exception;

import java.util.UUID;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(UUID id) {
        super("Customer with id " + id + " was not found.");
    }
}
