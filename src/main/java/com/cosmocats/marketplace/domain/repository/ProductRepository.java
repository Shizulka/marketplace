package com.cosmocats.marketplace.domain.repository;


import com.cosmocats.marketplace.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    List<Product> findAll();

    Optional<Product> findById(Long id);

    Optional<Product> update(Long id, Product product);

    boolean deleteById(Long id);
}