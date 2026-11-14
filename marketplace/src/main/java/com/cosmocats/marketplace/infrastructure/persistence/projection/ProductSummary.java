package com.cosmocats.marketplace.infrastructure.persistence.projection;

import java.math.BigDecimal;
import java.util.UUID;

/** Скорочений вигляд товару (лише потрібні стовпці). */
public interface ProductSummary {

    UUID getId();

    String getName();

    BigDecimal getPrice();
}
