package com.cosmocats.marketplace.infrastructure.repository;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final List<Product> products = new ArrayList<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    public InMemoryProductRepository() {
        products.add(new Product(
                idGenerator.getAndIncrement(),
                "Galaxy Yarn Ball",
                "Antigravity yarn ball for cosmic cats",
                new BigDecimal("19.99"),
                1L,
                10
        ));

        products.add(new Product(
                idGenerator.getAndIncrement(),
                "Cosmic Milk",
                "Fresh milk from the Milky Way",
                new BigDecimal("7.50"),
                2L,
                25
        ));

        products.add(new Product(
                idGenerator.getAndIncrement(),
                "Comet Toy",
                "Toy shaped like a flying comet",
                new BigDecimal("12.99"),
                3L,
                15
        ));
    }

    @Override
    public Product save(Product product) {
        product.setId(idGenerator.getAndIncrement());
        products.add(product);
        return product;
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Product> update(Long id, Product product) {
        Optional<Product> existingProduct = findById(id);

        if (existingProduct.isEmpty()) {
            return Optional.empty();
        }

        Product existing = existingProduct.get();

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategoryId(product.getCategoryId());
        existing.setStockQuantity(product.getStockQuantity());

        return Optional.of(existing);
    }

    @Override
    public boolean deleteById(Long id) {
        return products.removeIf(product -> product.getId().equals(id));
    }
}