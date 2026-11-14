package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.application.exception.CategoryNotFoundException;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSummary;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryJpaRepository categoryRepository;
    private final ProductJpaRepository productRepository;

    public CategoryService(CategoryJpaRepository categoryRepository,
                           ProductJpaRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public CategoryEntity create(String name, String description) {
        return categoryRepository.saveAndFlush(new CategoryEntity(name, description));
    }

    @Transactional(readOnly = true)
    public List<CategoryEntity> findAll() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public CategoryEntity findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryEntity> search(String namePart) {
        return categoryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(namePart);
    }

    public CategoryEntity update(Long id, String name, String description) {
        CategoryEntity category = findById(id);
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.saveAndFlush(category);
    }

    public void delete(Long id) {
        CategoryEntity category = findById(id);
        categoryRepository.delete(category);
        categoryRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<ProductSummary> findProductSummaries(Long categoryId) {
        findById(categoryId);
        return productRepository.findByCategoryIdOrderByPriceAsc(categoryId);
    }
}
