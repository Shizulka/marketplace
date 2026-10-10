package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.domain.repository.ProductRepository;
import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import org.springframework.stereotype.Service;
import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import java.util.List;
import java.util.UUID;

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
        long from = (long) page * size;
        List<Product> products = productRepository.findAll();
        int total = products.size();

        if (from >= total) {
            return new PagedResponse<>(
                    List.of(), page, size, total
            );
        }

        int fromIndex = (int) from;
        int toIndex = (int) Math.min(from + size, total);

        return new PagedResponse<>(
                products.subList(fromIndex, toIndex),
                page, size, total
        );
    }

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