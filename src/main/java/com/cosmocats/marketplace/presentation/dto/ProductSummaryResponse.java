package com.cosmocats.marketplace.presentation.dto;

import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSummary;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(UUID id, String name, BigDecimal price) {

    public static ProductSummaryResponse from(ProductSummary summary) {
        return new ProductSummaryResponse(summary.getId(), summary.getName(), summary.getPrice());
    }
}
