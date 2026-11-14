package com.cosmocats.marketplace.presentation.dto;

import java.util.UUID;

public record ProductSalesResponse(UUID productId, String productName, long totalQuantity) {
}
