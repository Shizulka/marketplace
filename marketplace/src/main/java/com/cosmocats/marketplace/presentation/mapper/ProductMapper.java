package com.cosmocats.marketplace.presentation.mapper;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.presentation.dto.ProductRequest;
import com.cosmocats.marketplace.presentation.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductRequest request) {
        return new Product(null, request.getName(), request.getDescription(), request.getPrice(), request.getCategoryId(), request.getStockQuantity()
        );
    }

    public ProductResponse toDto(Product product) {
        return new ProductResponse(product.getId(), product.getName(),product.getDescription(), product.getPrice(), product.getCategoryId(), product.getStockQuantity()
        );
    }

    public void updateDomain(Product product, ProductRequest request) {product.setName(request.getName());product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());product.setCategoryId(request.getCategoryId());product.setStockQuantity(request.getStockQuantity());
    }
}