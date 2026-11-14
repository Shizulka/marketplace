package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String namePart);
}
