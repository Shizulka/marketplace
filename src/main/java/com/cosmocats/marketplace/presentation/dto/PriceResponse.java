package com.cosmocats.marketplace.presentation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PriceResponse(UUID productId, String currency, BigDecimal price) {
}