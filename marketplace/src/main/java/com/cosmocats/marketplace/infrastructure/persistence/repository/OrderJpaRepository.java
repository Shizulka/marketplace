package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID>, OrderNaturalIdLookup {

    List<OrderEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status);
}
