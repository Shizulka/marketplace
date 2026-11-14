package com.cosmocats.marketplace;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.presentation.dto.ProductRequest;
import com.cosmocats.marketplace.presentation.dto.ProductResponse;

import com.cosmocats.marketplace.presentation.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    void toDomainShouldMapAllFields() {
        ProductRequest request = new ProductRequest();
        request.setName("Galaxy Yarn Ball");
        request.setDescription("Cosmic yarn ball");
        request.setPrice(new BigDecimal("19.99"));
        request.setCategoryId(1L);
        request.setStockQuantity(10);

        Product result = mapper.toDomain(request);

        assertNull(result.getId());
        assertEquals("Galaxy Yarn Ball", result.getName());
        assertEquals("Cosmic yarn ball", result.getDescription());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
        assertEquals(1L, result.getCategoryId());
        assertEquals(10, result.getStockQuantity());
    }

    @Test
    void toDomainShouldMapNullDescription() {
        ProductRequest request = new ProductRequest();
        request.setName("Cosmic Milk");
        request.setDescription(null);
        request.setPrice(new BigDecimal("7.50"));
        request.setCategoryId(2L);
        request.setStockQuantity(25);

        Product result = mapper.toDomain(request);

        assertNull(result.getDescription());
        assertEquals("Cosmic Milk", result.getName());
    }

    @Test
    void toDtoShouldMapAllFields() {
        UUID id = UUID.randomUUID();

        Product product = new Product(
                id,
                "Comet Toy",
                "Flying comet toy",
                new BigDecimal("12.99"),
                3L,
                15
        );

        ProductResponse result = mapper.toDto(product);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Comet Toy", result.getName());
        assertEquals("Flying comet toy", result.getDescription());
        assertEquals(new BigDecimal("12.99"), result.getPrice());
        assertEquals(3L, result.getCategoryId());
        assertEquals(15, result.getStockQuantity());
    }

    @Test
    void toDtoShouldMapNullDescription() {
        Product product = new Product(
                UUID.randomUUID(),
                "Cosmic Milk",
                null,
                new BigDecimal("7.50"),
                2L,
                25
        );

        ProductResponse result = mapper.toDto(product);

        assertNotNull(result);
        assertNull(result.getDescription());
        assertEquals("Cosmic Milk", result.getName());
    }

    @Test
    void updateDomainShouldUpdateAllFields() {
        UUID id = UUID.randomUUID();

        Product product = new Product(
                id,
                "Old name",
                "Old description",
                new BigDecimal("10.00"),
                1L,
                5
        );

        ProductRequest request = new ProductRequest();
        request.setName("New name");
        request.setDescription("New description");
        request.setPrice(new BigDecimal("29.99"));
        request.setCategoryId(2L);
        request.setStockQuantity(20);

        mapper.updateDomain(product, request);

        assertEquals(id, product.getId());
        assertEquals("New name", product.getName());
        assertEquals("New description", product.getDescription());
        assertEquals(new BigDecimal("29.99"), product.getPrice());
        assertEquals(2L, product.getCategoryId());
        assertEquals(20, product.getStockQuantity());
    }

    @Test
    void updateDomainShouldAllowNullDescription() {
        Product product = new Product(
                UUID.randomUUID(),
                "Old name",
                "Old description",
                new BigDecimal("10.00"),
                1L,
                5
        );

        ProductRequest request = new ProductRequest();
        request.setName("Updated name");
        request.setDescription(null);
        request.setPrice(new BigDecimal("20.00"));
        request.setCategoryId(2L);
        request.setStockQuantity(10);

        mapper.updateDomain(product, request);

        assertEquals("Updated name", product.getName());
        assertNull(product.getDescription());
    }

    @Test
    void updateDomainShouldPreserveProductId() {
        UUID id = UUID.randomUUID();

        Product product = new Product(
                id,
                "Old name",
                "Description",
                new BigDecimal("10.00"),
                1L,
                5
        );

        ProductRequest request = new ProductRequest();
        request.setName("Updated name");
        request.setDescription("Updated description");
        request.setPrice(new BigDecimal("15.00"));
        request.setCategoryId(2L);
        request.setStockQuantity(10);

        mapper.updateDomain(product, request);

        assertEquals(id, product.getId());
    }

    @Test
    void updateDomainShouldAllowZeroStock() {
        Product product = new Product(
                UUID.randomUUID(),
                "Comet Toy",
                "Toy description",
                new BigDecimal("12.99"),
                3L,
                15
        );

        ProductRequest request = new ProductRequest();
        request.setName("Comet Toy");
        request.setDescription("Toy description");
        request.setPrice(new BigDecimal("12.99"));
        request.setCategoryId(3L);
        request.setStockQuantity(0);

        mapper.updateDomain(product, request);

        assertEquals(0, product.getStockQuantity());
    }
}
