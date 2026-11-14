package com.cosmocats.marketplace.presentation.controller;

import com.cosmocats.marketplace.application.service.CategoryService;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.presentation.dto.CategoryRequest;
import com.cosmocats.marketplace.presentation.dto.CategoryResponse;
import com.cosmocats.marketplace.presentation.dto.ProductSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryEntity created = categoryService.create(request.name(), request.description());
        return ResponseEntity
                .created(URI.create("/api/categories/" + created.getId()))
                .body(CategoryResponse.from(created));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(
            @RequestParam(required = false) String search) {
        List<CategoryEntity> categories = (search == null || search.isBlank())
                ? categoryService.findAll()
                : categoryService.search(search);
        return ResponseEntity.ok(categories.stream().map(CategoryResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.findById(id)));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductSummaryResponse>> findProducts(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findProductSummaries(id).stream()
                .map(ProductSummaryResponse::from)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(
                categoryService.update(id, request.name(), request.description())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
