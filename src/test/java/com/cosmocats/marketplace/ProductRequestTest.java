package com.cosmocats.marketplace;


import com.cosmocats.marketplace.presentation.dto.ProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private ProductRequest createValidRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming laptop");
        request.setPrice(new BigDecimal("999.99"));
        request.setCategoryId(1L);
        request.setStockQuantity(10);
        return request;
    }

    @Test
    void shouldPassValidationWhenAllFieldsAreValid() {
        ProductRequest request = createValidRequest();

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenNameIsNull() {
        ProductRequest request = createValidRequest();
        request.setName(null);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "name"));
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        ProductRequest request = createValidRequest();
        request.setName("   ");

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "name"));
    }

    @Test
    void shouldFailValidationWhenNameIsTooShort() {
        ProductRequest request = createValidRequest();
        request.setName("AB");

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "name"));
    }

    @Test
    void shouldFailValidationWhenNameIsTooLong() {
        ProductRequest request = createValidRequest();
        request.setName("A".repeat(101));

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "name"));
    }

    @Test
    void shouldPassValidationWhenNameHasMinimumLength() {
        ProductRequest request = createValidRequest();
        request.setName("ABC");

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "name"));
    }

    @Test
    void shouldPassValidationWhenNameHasMaximumLength() {
        ProductRequest request = createValidRequest();
        request.setName("A".repeat(100));

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "name"));
    }

    @Test
    void shouldFailValidationWhenDescriptionIsTooLong() {
        ProductRequest request = createValidRequest();
        request.setDescription("A".repeat(501));

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "description"));
    }

    @Test
    void shouldPassValidationWhenDescriptionIsNull() {
        ProductRequest request = createValidRequest();
        request.setDescription(null);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "description"));
    }

    @Test
    void shouldPassValidationWhenDescriptionHasMaximumLength() {
        ProductRequest request = createValidRequest();
        request.setDescription("A".repeat(500));

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "description"));
    }

    @Test
    void shouldFailValidationWhenPriceIsNull() {
        ProductRequest request = createValidRequest();
        request.setPrice(null);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "price"));
    }

    @Test
    void shouldFailValidationWhenPriceIsZero() {
        ProductRequest request = createValidRequest();
        request.setPrice(BigDecimal.ZERO);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "price"));
    }

    @Test
    void shouldFailValidationWhenPriceIsNegative() {
        ProductRequest request = createValidRequest();
        request.setPrice(new BigDecimal("-10.00"));

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "price"));
    }

    @Test
    void shouldPassValidationWhenPriceIsMinimumAllowed() {
        ProductRequest request = createValidRequest();
        request.setPrice(new BigDecimal("0.01"));

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "price"));
    }

    @Test
    void shouldFailValidationWhenCategoryIdIsNull() {
        ProductRequest request = createValidRequest();
        request.setCategoryId(null);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "categoryId"));
    }

    @Test
    void shouldFailValidationWhenCategoryIdIsZero() {
        ProductRequest request = createValidRequest();
        request.setCategoryId(0L);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "categoryId"));
    }

    @Test
    void shouldFailValidationWhenCategoryIdIsNegative() {
        ProductRequest request = createValidRequest();
        request.setCategoryId(-1L);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "categoryId"));
    }

    @Test
    void shouldFailValidationWhenStockQuantityIsNull() {
        ProductRequest request = createValidRequest();
        request.setStockQuantity(null);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "stockQuantity"));
    }

    @Test
    void shouldFailValidationWhenStockQuantityIsNegative() {
        ProductRequest request = createValidRequest();
        request.setStockQuantity(-1);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolation(violations, "stockQuantity"));
    }

    @Test
    void shouldPassValidationWhenStockQuantityIsZero() {
        ProductRequest request = createValidRequest();
        request.setStockQuantity(0);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "stockQuantity"));
    }

    @Test
    void shouldPassValidationWhenCategoryIdIsPositive() {
        ProductRequest request = createValidRequest();
        request.setCategoryId(1L);

        Set<ConstraintViolation<ProductRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolation(violations, "categoryId"));
    }

    private boolean hasViolation(
            Set<ConstraintViolation<ProductRequest>> violations,
            String propertyName
    ) {
        return violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals(propertyName));
    }
}
