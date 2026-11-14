package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import com.cosmocats.marketplace.domain.repository.ProductPage;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Product> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }
        ProductPage result = productRepository.findPage(page, size);
        return new PagedResponse<>(result.content(), page, size, result.totalElements());
    }

    @Transactional(readOnly = true)
    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    public Product update(UUID id, Product product) {
        return productRepository.update(id, product)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    public void delete(UUID id) {
        boolean deleted = productRepository.deleteById(id);

        if (!deleted) {
            throw new ProductNotFoundException(id);
        }
    }
}