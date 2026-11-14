package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.InsufficientStockException;
import com.cosmocats.marketplace.application.service.OrderService;
import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.*;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Без @Transactional на тесті: перевіряємо, що саме @Transactional сервісу відкочує зміни. */
@SpringBootTest
class OrderServiceRollbackTest extends AbstractPostgresTest {

    @Autowired private OrderService orderService;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ProductJpaRepository productRepository;
    @Autowired private CustomerJpaRepository customerRepository;
    @Autowired private OrderJpaRepository orderRepository;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void failedOrderShouldRollbackStockOfEarlierItems() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Rollback", null));
        ProductEntity plenty = productRepository.save(
                new ProductEntity("Plenty", "d", BigDecimal.TEN, category, 10));
        ProductEntity scarce = productRepository.save(
                new ProductEntity("Scarce", "d", BigDecimal.TEN, category, 1));
        CustomerEntity customer = customerRepository.save(
                new CustomerEntity("rollback@cosmo.cats", "Rollback Cat"));

        assertThrows(InsufficientStockException.class, () -> orderService.create(
                customer.getId(),
                List.of(new OrderLine(plenty.getId(), 3), new OrderLine(scarce.getId(), 2))));

        assertEquals(10, productRepository.findById(plenty.getId()).orElseThrow().getStockQuantity());
        assertEquals(1, productRepository.findById(scarce.getId()).orElseThrow().getStockQuantity());
        assertEquals(0, orderRepository.count());
    }
}
