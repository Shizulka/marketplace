package com.cosmocats.marketplace.infrastructure.persistence.projection;

import java.util.UUID;

/** Звіт: скільки одиниць кожного товару куплено. */
public interface ProductSalesProjection {

    UUID getProductId();

    String getProductName();

    Long getTotalQuantity();
}
