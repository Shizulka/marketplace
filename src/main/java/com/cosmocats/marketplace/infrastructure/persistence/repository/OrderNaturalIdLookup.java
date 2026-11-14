package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;

import java.util.Optional;

/** Custom-фрагмент репозиторію: пошук замовлення за Natural ID (orderNumber). */
public interface OrderNaturalIdLookup {

    Optional<OrderEntity> loadByOrderNumber(String orderNumber);
}
