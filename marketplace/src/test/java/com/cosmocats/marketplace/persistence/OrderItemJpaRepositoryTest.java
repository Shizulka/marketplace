package com.cosmocats.marketplace.persistence;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.*;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSalesProjection;
import com.cosmocats.marketplace.infrastructure.persistence.repository.*;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.cosmocats.marketplace.support.PostgresDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Звіт "найпопулярніші товари": JPQL + projection на справжньому PostgreSQL. */
@PostgresDataJpaTest
class OrderItemJpaRepositoryTest extends AbstractPostgresTest {

    @Autowired private OrderItemJpaRepository repository;
    @Autowired private OrderJpaRepository orderRepository;
    @Autowired private CustomerJpaRepository customerRepository;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ProductJpaRepository productRepository;

    @BeforeEach
    void setUp() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Toys", null));
        ProductEntity yarn = productRepository.save(new ProductEntity("Yarn", "d", BigDecimal.TEN, category, 100));
        ProductEntity milk = productRepository.save(new ProductEntity("Milk", "d", BigDecimal.ONE, category, 100));
        ProductEntity comet = productRepository.save(new ProductEntity("Comet", "d", BigDecimal.TEN, category, 100));
        CustomerEntity customer = customerRepository.save(new CustomerEntity("c@cosmo.cats", "Cat"));

        save("CC-A", customer, OrderStatus.PAID, item(yarn, 2), item(milk, 5));
        save("CC-B", customer, OrderStatus.NEW, item(yarn, 4), item(comet, 5));
        save("CC-C", customer, OrderStatus.CANCELLED, item(comet, 50));
    }

    private OrderItemEntity item(ProductEntity product, int quantity) {
        return new OrderItemEntity(product, quantity, product.getPrice());
    }

    private void save(String number, CustomerEntity customer, OrderStatus status, OrderItemEntity... items) {
        OrderEntity order = new OrderEntity(number, customer);
        order.setStatus(status);
        for (OrderItemEntity item : items) {
            order.addItem(item);
        }
        orderRepository.saveAndFlush(order);
    }

    @Test
    void shouldAggregateSortAndSkipCancelledOrders() {
        List<ProductSalesProjection> report = repository.findTopSoldProducts(
                OrderStatus.CANCELLED, Instant.EPOCH, PageRequest.of(0, 10));

        assertEquals(List.of("Yarn", "Comet", "Milk"),
                report.stream().map(ProductSalesProjection::getProductName).toList());
        assertEquals(List.of(6L, 5L, 5L),
                report.stream().map(ProductSalesProjection::getTotalQuantity).toList());
        assertNotNull(report.get(0).getProductId());
    }

    @Test
    void shouldApplyPageSize() {
        List<ProductSalesProjection> report = repository.findTopSoldProducts(
                OrderStatus.CANCELLED, Instant.EPOCH, PageRequest.of(0, 1));

        assertEquals(1, report.size());
        assertEquals("Yarn", report.get(0).getProductName());
    }

    @Test
    void shouldFilterBySinceDate() {
        assertTrue(repository.findTopSoldProducts(
                OrderStatus.CANCELLED, Instant.now().plusSeconds(3600), PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void excludedStatusShouldBeConfigurable() {
        List<ProductSalesProjection> report = repository.findTopSoldProducts(
                OrderStatus.NEW, Instant.EPOCH, PageRequest.of(0, 10));

        // без замовлення CC-B (NEW), але разом зі скасованим CC-C: Comet = 50
        assertEquals("Comet", report.get(0).getProductName());
        assertEquals(50L, report.get(0).getTotalQuantity());
    }
}
