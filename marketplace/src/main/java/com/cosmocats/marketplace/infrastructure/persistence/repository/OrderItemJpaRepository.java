package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderItemEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSalesProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {

    /**
     * Звіт для Інтергалактичного комітету: найпопулярніші товари.
     * JPQL (не native), WHERE (статус, дата), GROUP BY, ORDER BY; повертає projection.
     */
    @Query("""
            SELECT p.id AS productId,
                   p.name AS productName,
                   SUM(oi.quantity) AS totalQuantity
            FROM OrderItemEntity oi
            JOIN oi.product p
            JOIN oi.purchaseOrder o
            WHERE o.status <> :excludedStatus
              AND o.createdAt >= :since
            GROUP BY p.id, p.name
            ORDER BY SUM(oi.quantity) DESC, p.name ASC
            """)
    List<ProductSalesProjection> findTopSoldProducts(
            @Param("excludedStatus") OrderStatus excludedStatus,
            @Param("since") Instant since,
            Pageable pageable);
}
