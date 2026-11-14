package com.cosmocats.marketplace.infrastructure.persistence.repository;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
}
