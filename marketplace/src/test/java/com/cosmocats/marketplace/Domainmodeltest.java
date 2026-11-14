package com.cosmocats.marketplace;

import com.cosmocats.marketplace.domain.model.Cart;
import com.cosmocats.marketplace.domain.model.Category;
import com.cosmocats.marketplace.domain.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Прості unit-тести доменних моделей (без Spring і без БД). */
class DomainModelTest {

    @Test
    void categoryShouldExposeConstructorValuesAndAllowChanges() {
        Category category = new Category(1L, "Toys", "Toys for cats",
                new BigDecimal("9.99"), 1L, 5);

        assertEquals(1L, category.getId());
        assertEquals("Toys", category.getName());
        assertEquals("Toys for cats", category.getDescription());

        category.setId(2L);
        category.setName("Food");
        category.setDescription("Galaxy food");

        assertEquals(2L, category.getId());
        assertEquals("Food", category.getName());
        assertEquals("Galaxy food", category.getDescription());
    }

    @Test
    void cartShouldExposeConstructorValues() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        List<Long> productIds = List.of(1L, 2L);

        Cart cart = new Cart(id, customerId, productIds);

        assertEquals(id, cart.getId());
        assertEquals(customerId, cart.getCustomerId());
        assertEquals(productIds, cart.getProductIds());
    }

    @Test
    void cartShouldAllowChangesThroughSetters() {
        Cart cart = new Cart();
        assertNull(cart.getId());
        assertNull(cart.getCustomerId());
        assertNull(cart.getProductIds());

        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        cart.setId(id);
        cart.setCustomerId(customerId);
        cart.setProductIds(List.of(3L));

        assertEquals(id, cart.getId());
        assertEquals(customerId, cart.getCustomerId());
        assertEquals(List.of(3L), cart.getProductIds());
    }

    @Test
    void orderShouldExposeConstructorValues() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        List<Long> productIds = List.of(1L, 2L);

        Order order = new Order(id, customerId, productIds, new BigDecimal("25.00"), "NEW");

        assertEquals(id, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(productIds, order.getProductIds());
        assertEquals(new BigDecimal("25.00"), order.getTotalPrice());
        assertEquals("NEW", order.getStatus());
    }

    @Test
    void orderShouldAllowChangesThroughSetters() {
        Order order = new Order();
        assertNull(order.getId());
        assertNull(order.getCustomerId());
        assertNull(order.getProductIds());
        assertNull(order.getTotalPrice());
        assertNull(order.getStatus());

        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        order.setId(id);
        order.setCustomerId(customerId);
        order.setProductIds(List.of(7L));
        order.setTotalPrice(BigDecimal.TEN);
        order.setStatus("PAID");

        assertEquals(id, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(List.of(7L), order.getProductIds());
        assertEquals(BigDecimal.TEN, order.getTotalPrice());
        assertEquals("PAID", order.getStatus());
    }
}