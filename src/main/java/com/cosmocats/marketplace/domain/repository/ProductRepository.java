package com.cosmocats.marketplace.domain.repository;


import com.cosmocats.marketplace.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    List<Product> findAll();

    Optional<Product> findById(UUID id);

    Optional<Product> update(UUID id, Product product);

    boolean deleteById(UUID id);
}