package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CustomerNotFoundException;
import com.cosmocats.marketplace.application.exception.InsufficientStockException;
import com.cosmocats.marketplace.application.exception.OrderNotFoundException;
import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import com.cosmocats.marketplace.application.service.OrderService;
import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CustomerJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest extends AbstractPostgresTest {

    @Autowired private OrderService orderService;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ProductJpaRepository productRepository;
    @Autowired private CustomerJpaRepository customerRepository;

    private CustomerEntity customer;
    private ProductEntity yarn;
    private ProductEntity milk;

    @BeforeEach
    void setUp() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Toys", null));
        yarn = productRepository.save(new ProductEntity("Yarn", "d", new BigDecimal("10.00"), category, 10));
        milk = productRepository.save(new ProductEntity("Milk", "d", new BigDecimal("2.50"), category, 4));
        customer = customerRepository.save(new CustomerEntity("cat@cosmo.cats", "Space Cat"));
    }

    @Test
    void createShouldSnapshotPricesMergeDuplicatesAndDecreaseStock() {
        OrderEntity order = orderService.create(customer.getId(), List.of(
                new OrderLine(yarn.getId(), 2),
                new OrderLine(yarn.getId(), 1),
                new OrderLine(milk.getId(), 4)));

        assertNotNull(order.getId());
        assertTrue(order.getOrderNumber().matches("CC-[0-9A-F]{10}"));
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(2, order.getItems().size());
        assertEquals(0, new BigDecimal("40.00").compareTo(order.getTotalPrice()));
        assertEquals(7, yarn.getStockQuantity());
        assertEquals(0, milk.getStockQuantity());
        assertNotNull(order.getCreatedAt());
        assertEquals(customer.getId(), order.getCustomer().getId());
    }

    @Test
    void createShouldRejectEmptyOrNonPositiveLines() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(customer.getId(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(customer.getId(), null));
        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 0))));
        assertThrows(ArithmeticException.class,
                () -> orderService.create(customer.getId(), List.of(
                        new OrderLine(yarn.getId(), Integer.MAX_VALUE),
                        new OrderLine(yarn.getId(), 1))));
    }

    @Test
    void createShouldFailForUnknownCustomerProductOrInsufficientStock() {
        assertThrows(CustomerNotFoundException.class,
                () -> orderService.create(UUID.randomUUID(), List.of(new OrderLine(yarn.getId(), 1))));
        assertThrows(ProductNotFoundException.class,
                () -> orderService.create(customer.getId(), List.of(new OrderLine(UUID.randomUUID(), 1))));
        assertThrows(InsufficientStockException.class,
                () -> orderService.create(customer.getId(), List.of(new OrderLine(milk.getId(), 5))));
    }

    @Test
    void findByIdShouldReturnOrderOrThrow() {
        OrderEntity order = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 1)));

        assertEquals(order.getId(), orderService.findById(order.getId()).getId());
        assertThrows(OrderNotFoundException.class, () -> orderService.findById(UUID.randomUUID()));
    }

    @Test
    void findByOrderNumberShouldUseNaturalId() {
        OrderEntity order = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 1)));

        OrderEntity found = orderService.findByOrderNumber(order.getOrderNumber());

        assertEquals(order.getId(), found.getId());
        assertThrows(OrderNotFoundException.class, () -> orderService.findByOrderNumber("CC-NOPE"));
    }

    @Test
    void findAllAndFindByStatusShouldFilterOrders() {
        OrderEntity first = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 1)));
        orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 1)));
        orderService.updateStatus(first.getId(), OrderStatus.PAID);

        assertEquals(2, orderService.findAll().size());
        assertEquals(1, orderService.findByStatus(OrderStatus.PAID).size());
        assertEquals(1, orderService.findByStatus(OrderStatus.NEW).size());
        assertTrue(orderService.findByStatus(OrderStatus.SHIPPED).isEmpty());
    }

    @Test
    void cancellingShouldRestoreStockAndCannotBeRepeated() {
        OrderEntity order = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 3)));
        assertEquals(7, yarn.getStockQuantity());

        OrderEntity cancelled = orderService.updateStatus(order.getId(), OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(10, yarn.getStockQuantity());
        assertThrows(IllegalStateException.class,
                () -> orderService.updateStatus(order.getId(), OrderStatus.PAID));
    }

    @Test
    void deleteShouldRemoveOrderAndRestoreReservedStock() {
        OrderEntity order = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 1)));
        assertEquals(9, yarn.getStockQuantity());

        orderService.delete(order.getId());

        assertEquals(10, yarn.getStockQuantity());
        assertThrows(OrderNotFoundException.class, () -> orderService.findById(order.getId()));
    }

    @Test
    void deletingCancelledOrderMustNotRestoreStockTwice() {
        OrderEntity order = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 2)));
        orderService.updateStatus(order.getId(), OrderStatus.CANCELLED);
        assertEquals(10, yarn.getStockQuantity());

        orderService.delete(order.getId());

        assertEquals(10, yarn.getStockQuantity());
    }

    @Test
    void statusTransitionsMustFollowWorkflow() {
        OrderEntity order = orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 1)));
        orderService.updateStatus(order.getId(), OrderStatus.PAID);
        orderService.updateStatus(order.getId(), OrderStatus.SHIPPED);
        orderService.updateStatus(order.getId(), OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class,
                () -> orderService.updateStatus(order.getId(), OrderStatus.CANCELLED));
    }
}
