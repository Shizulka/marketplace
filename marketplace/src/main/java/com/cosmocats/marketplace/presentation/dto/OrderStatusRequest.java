package com.cosmocats.marketplace.presentation.dto;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status) {
}
