package com.cosmocats.marketplace.domain.repository;

import com.cosmocats.marketplace.domain.model.Product;

import java.util.List;

/** A framework-independent slice of products and the total matching row count. */
public record ProductPage(List<Product> content, long totalElements) {
    public ProductPage {
        content = List.copyOf(content);
    }
}
