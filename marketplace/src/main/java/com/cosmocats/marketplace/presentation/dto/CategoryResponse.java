package com.cosmocats.marketplace.presentation.dto;

import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;

public record CategoryResponse(Long id, String name, String description) {

    public static CategoryResponse from(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
