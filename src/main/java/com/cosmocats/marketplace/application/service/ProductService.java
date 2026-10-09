package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import org.springframework.stereotype.Service;
import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public PagedResponse<Product> findAll(int page, int size) {
        List<Product> products = productRepository.findAll();

        int totalElements = products.size();

        int fromIndex = page * size;

        if (fromIndex >= totalElements) {
            return new PagedResponse<>(
                    List.of(),
                    page,
                    size,
                    totalElements
            );
        }

        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Product> pagedProducts = products.subList(fromIndex, toIndex);

        return new PagedResponse<>(
                pagedProducts,
                page,
                size,
                totalElements
        );
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    public Product update(Long id, Product product) {
        return productRepository.update(id, product)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );
    }

    public void delete(Long id) {
        boolean deleted = productRepository.deleteById(id);

        if (!deleted) {
            throw new ProductNotFoundException(id);
        }
    }
}