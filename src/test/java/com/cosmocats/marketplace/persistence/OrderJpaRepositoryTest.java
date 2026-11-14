package com.cosmocats.marketplace.persistence;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.*;
import com.cosmocats.marketplace.infrastructure.persistence.repository.*;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.cosmocats.marketplace.support.PostgresDataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@PostgresDataJpaTest
class OrderJpaRepositoryTest extends AbstractPostgresTest {

    @Autowired private OrderJpaRepository repository;
    @Autowired private CustomerJpaRepository customerRepository;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ProductJpaRepository productRepository;
    @PersistenceContext private EntityManager em;

    private CustomerEntity customer;
    private ProductEntity yarn;
    private ProductEntity milk;

    @BeforeEach
    void setUp() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Toys", null));
        yarn = productRepository.save(new ProductEntity("Yarn", "d", new BigDecimal("10.00"), category, 10));
        milk = productRepository.save(new ProductEntity("Milk", "d", new BigDecimal("2.50"), category, 10));
        customer = customerRepository.save(new CustomerEntity("cat@cosmo.cats", "Space Cat"));
        em.flush();
    }

    private OrderEntity order(String number) {
        OrderEntity order = new OrderEntity(number, customer);
        order.addItem(new OrderItemEntity(yarn, 2, yarn.getPrice()));
        order.addItem(new OrderItemEntity(milk, 1, milk.getPrice()));
        return order;
    }

    private long countItems() {
        return em.createQuery("SELECT COUNT(i) FROM OrderItemEntity i", Long.class).getSingleResult();
    }

    @Test
    void createShouldCascadeItemsAndGenerateIds() {
        UUID id = repository.saveAndFlush(order("CC-0000000001")).getId();
        em.clear();

        OrderEntity loaded = repository.findById(id).orElseThrow();

        assertEquals(OrderStatus.NEW, loaded.getStatus());
        assertNotNull(loaded.getCreatedAt());
        assertEquals(2, loaded.getItems().size());
        assertTrue(loaded.getItems().stream().allMatch(i -> i.getId() != null));
        assertEquals(0, new BigDecimal("22.50").compareTo(loaded.getTotalPrice()));
        assertEquals(customer.getId(), loaded.getCustomer().getId());
    }

    @Test
    void updateStatusShouldBePersisted() {
        UUID id = repository.saveAndFlush(order("CC-0000000002")).getId();
        em.clear();

        repository.findById(id).orElseThrow().setStatus(OrderStatus.PAID);
        repository.flush();
        em.clear();

        assertEquals(OrderStatus.PAID, repository.findById(id).orElseThrow().getStatus());
    }

    @Test
    void naturalIdLookupShouldFindOrderByOrderNumber() {
        UUID id = repository.saveAndFlush(order("CC-0000000003")).getId();
        em.clear();

        assertEquals(id, repository.loadByOrderNumber("CC-0000000003").orElseThrow().getId());
        assertTrue(repository.loadByOrderNumber("CC-UNKNOWN").isEmpty());
    }

    @Test
    void duplicateOrderNumberShouldViolateUniqueConstraint() {
        repository.saveAndFlush(order("CC-0000000004"));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(order("CC-0000000004")));
    }

    @Test
    void sameProductTwiceInOneOrderShouldViolateUniqueConstraint() {
        OrderEntity order = new OrderEntity("CC-0000000005", customer);
        order.addItem(new OrderItemEntity(yarn, 1, yarn.getPrice()));
        order.addItem(new OrderItemEntity(yarn, 2, yarn.getPrice()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(order));
    }

    @Test
    void nonPositiveQuantityShouldViolateCheckConstraint() {
        OrderEntity order = new OrderEntity("CC-0000000006", customer);
        order.addItem(new OrderItemEntity(yarn, 0, yarn.getPrice()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(order));
    }

    @Test
    void findByStatusShouldFilterAndSortNewestFirst() throws InterruptedException {
        repository.saveAndFlush(order("CC-0000000007"));
        Thread.sleep(5);
        repository.saveAndFlush(order("CC-0000000008"));
        Thread.sleep(5);
        OrderEntity paid = order("CC-0000000009");
        paid.setStatus(OrderStatus.PAID);
        repository.saveAndFlush(paid);

        List<String> newOrders = repository.findByStatusOrderByCreatedAtDesc(OrderStatus.NEW)
                .stream().map(OrderEntity::getOrderNumber).toList();

        assertEquals(List.of("CC-0000000008", "CC-0000000007"), newOrders);
        assertEquals(1, repository.findByStatusOrderByCreatedAtDesc(OrderStatus.PAID).size());
        assertTrue(repository.findByStatusOrderByCreatedAtDesc(OrderStatus.SHIPPED).isEmpty());
    }

    @Test
    void removingItemFromOrderShouldDeleteOrphan() {
        OrderEntity saved = repository.saveAndFlush(order("CC-0000000010"));
        assertEquals(2, countItems());

        saved.getItems().remove(0);
        repository.flush();

        assertEquals(1, countItems());
    }

    @Test
    void deletingOrderShouldDeleteItsItems() {
        OrderEntity saved = repository.saveAndFlush(order("CC-0000000011"));

        repository.delete(saved);
        repository.flush();

        assertEquals(0, countItems());
        assertFalse(repository.existsById(saved.getId()));
    }
}
