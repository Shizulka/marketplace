package com.cosmocats.marketplace.presentation.controller;

import com.cosmocats.marketplace.application.service.ProductService;
import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.presentation.dto.PagedResponse;
import com.cosmocats.marketplace.presentation.dto.ProductRequest;
import com.cosmocats.marketplace.presentation.dto.ProductResponse;
import com.cosmocats.marketplace.presentation.mapper.ProductMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(
            ProductService productService,
            ProductMapper productMapper
    ) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request
    ) {
        Product product = productMapper.toDomain(request);

        Product createdProduct = productService.create(product);

        ProductResponse response = productMapper.toDto(createdProduct);

        return ResponseEntity
                .created(URI.create("/api/products/" + createdProduct.getId()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        PagedResponse<Product> products =
                productService.findAll(page, size);

        PagedResponse<ProductResponse> response =
                new PagedResponse<>(
                        products.getContent()
                                .stream()
                                .map(productMapper::toDto)
                                .toList(),
                        products.getPage(),
                        products.getSize(),
                        products.getTotalElements()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @PathVariable UUID id
    ) {
        Product product = productService.findById(id);

        return ResponseEntity.ok(
                productMapper.toDto(product)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        Product product = productMapper.toDomain(request);

        Product updatedProduct =
                productService.update(id, product);

        return ResponseEntity.ok(
                productMapper.toDto(updatedProduct)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        productService.delete(id);

        return ResponseEntity.noContent().build();
    }
}