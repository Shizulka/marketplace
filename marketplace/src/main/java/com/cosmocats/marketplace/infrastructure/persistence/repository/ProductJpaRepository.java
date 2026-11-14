package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    /** Named-метод + projection: WHERE category.id = ? ORDER BY price ASC, лише id/name/price. */
    List<ProductSummary> findByCategoryIdOrderByPriceAsc(Long categoryId);

    /** Блокування рядка товару на час транзакції, щоб два замовлення не "продали" той самий залишок. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    Optional<ProductEntity> findByIdForUpdate(@Param("id") UUID id);
}
