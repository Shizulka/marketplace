package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

import java.util.Optional;

public class OrderNaturalIdLookupImpl implements OrderNaturalIdLookup {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<OrderEntity> loadByOrderNumber(String orderNumber) {
        return Optional.ofNullable(
                entityManager.unwrap(Session.class)
                        .bySimpleNaturalId(OrderEntity.class)
                        .load(orderNumber));
    }
}
