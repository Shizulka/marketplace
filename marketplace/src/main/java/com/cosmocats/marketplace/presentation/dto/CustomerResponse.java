package com.cosmocats.marketplace.presentation.dto;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(UUID id, String email, String fullName, Instant createdAt) {

    public static CustomerResponse from(CustomerEntity customer) {
        return new CustomerResponse(
                customer.getId(), customer.getEmail(), customer.getFullName(), customer.getCreatedAt());
    }
}
