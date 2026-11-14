package com.cosmocats.marketplace;

import com.cosmocats.marketplace.presentation.dto.ProductResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    @Test
    void noArgsConstructorShouldCreateResponseWithNullFields() {
        ProductResponse response = new ProductResponse();

        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getDescription());
        assertNull(response.getPrice());
        assertNull(response.getCategoryId());
        assertNull(response.getStockQuantity());
    }

    @Test
    void allArgsConstructorShouldSetAllFields() {
        UUID id = UUID.randomUUID();

        ProductResponse response = new ProductResponse(
                id, "Galaxy Yarn", "Yarn", new BigDecimal("19.99"), 2L, 7);

        assertEquals(id, response.getId());
        assertEquals("Galaxy Yarn", response.getName());
        assertEquals("Yarn", response.getDescription());
        assertEquals(new BigDecimal("19.99"), response.getPrice());
        assertEquals(2L, response.getCategoryId());
        assertEquals(7, response.getStockQuantity());
    }

    @Test
    void settersShouldUpdateAllFields() {
        ProductResponse response = new ProductResponse();
        UUID id = UUID.randomUUID();

        response.setId(id);
        response.setName("Comet Toy");
        response.setDescription("Flying toy");
        response.setPrice(new BigDecimal("12.99"));
        response.setCategoryId(3L);
        response.setStockQuantity(15);

        assertEquals(id, response.getId());
        assertEquals("Comet Toy", response.getName());
        assertEquals("Flying toy", response.getDescription());
        assertEquals(new BigDecimal("12.99"), response.getPrice());
        assertEquals(3L, response.getCategoryId());
        assertEquals(15, response.getStockQuantity());
    }

    @Test
    void settersShouldAllowNullValues() {
        ProductResponse response = new ProductResponse(
                UUID.randomUUID(), "Name", "Desc", BigDecimal.ONE, 1L, 1);

        response.setId(null);
        response.setName(null);
        response.setDescription(null);
        response.setPrice(null);
        response.setCategoryId(null);
        response.setStockQuantity(null);

        assertNull(response.getId());
        assertNull(response.getName());
        assertNull(response.getDescription());
        assertNull(response.getPrice());
        assertNull(response.getCategoryId());
        assertNull(response.getStockQuantity());
    }
}