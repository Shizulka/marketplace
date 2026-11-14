package com.cosmocats.marketplace.infrastructure.repository;

import com.cosmocats.marketplace.application.exception.CategoryNotFoundException;
import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import com.cosmocats.marketplace.domain.repository.ProductPage;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Реалізація доменного порту ProductRepository поверх Spring Data JPA. */
@Repository
@Transactional
public class JpaProductRepository implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    public JpaProductRepository(ProductJpaRepository productJpaRepository,
                                CategoryJpaRepository categoryJpaRepository) {
        this.productJpaRepository = productJpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = new ProductEntity(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                findCategory(product.getCategoryId()),
                product.getStockQuantity());
        return toDomain(productJpaRepository.saveAndFlush(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productJpaRepository.findAll(Sort.by("createdAt", "id"))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPage findPage(int page, int size) {
        org.springframework.data.domain.Page<ProductEntity> result = productJpaRepository.findAll(
                org.springframework.data.domain.PageRequest.of(page, size, Sort.by("createdAt", "id")));
        return new ProductPage(result.getContent().stream().map(this::toDomain).toList(),
                result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Product> update(UUID id, Product product) {
        return productJpaRepository.findById(id).map(entity -> {
            entity.setName(product.getName());
            entity.setDescription(product.getDescription());
            entity.setPrice(product.getPrice());
            entity.setCategory(findCategory(product.getCategoryId()));
            entity.setStockQuantity(product.getStockQuantity());
            return toDomain(productJpaRepository.saveAndFlush(entity));
        });
    }

    @Override
    public boolean deleteById(UUID id) {
        if (!productJpaRepository.existsById(id)) {
            return false;
        }
        productJpaRepository.deleteById(id);
        productJpaRepository.flush();
        return true;
    }

    private CategoryEntity findCategory(Long categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private Product toDomain(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getCategory().getId(),
                entity.getStockQuantity());
    }
}
