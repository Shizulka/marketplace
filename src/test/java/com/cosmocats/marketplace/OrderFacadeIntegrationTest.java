package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CustomerNotFoundException;
import com.cosmocats.marketplace.application.service.CustomerService;
import com.cosmocats.marketplace.application.service.OrderDetails;
import com.cosmocats.marketplace.application.service.OrderFacade;
import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CategoryJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CustomerJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.OrderJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import com.cosmocats.marketplace.support.AbstractPostgresTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Без @Transactional на тесті: перевіряє, що OrderFacade читає lazy-зв'язки (позиції, товари,
 * покупець) саме у власній транзакції, як це буде в REST-запиті з вимкненим open-in-view.
 */
@SpringBootTest
class OrderFacadeIntegrationTest extends AbstractPostgresTest {

    @Autowired private OrderFacade orderFacade;
    @Autowired private CustomerService customerService;
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
    void facadeShouldCreateReadUpdateAndDeleteOrderWithItems() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Facade", null));
        ProductEntity yarn = productRepository.save(
                new ProductEntity("Yarn", "d", new BigDecimal("10.00"), category, 10));
        CustomerEntity customer = customerService.create("facade@cosmo.cats", "Facade Cat");

        OrderDetails created = orderFacade.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 3)));

        assertNotNull(created.id());
        assertEquals(customer.getId(), created.customerId());
        assertEquals(OrderStatus.NEW, created.status());
        assertEquals(1, created.items().size());
        assertEquals("Yarn", created.items().get(0).productName());
        assertEquals(0, new BigDecimal("30.00").compareTo(created.totalPrice()));

        assertEquals(created.orderNumber(), orderFacade.findById(created.id()).orderNumber());
        assertEquals(created.id(), orderFacade.findByOrderNumber(created.orderNumber()).id());
        assertEquals(1, orderFacade.findAll().size());
        assertEquals(1, orderFacade.findByStatus(OrderStatus.NEW).size());
        assertEquals(0, orderFacade.findByStatus(OrderStatus.PAID).size());

        OrderDetails paid = orderFacade.updateStatus(created.id(), OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, paid.status());
        assertEquals(1, paid.items().size());

        orderFacade.delete(created.id());
        assertEquals(0, orderFacade.findAll().size());
    }

    @Test
    void customerServiceShouldCreateFindAndDeleteCustomers() {
        CustomerEntity created = customerService.create("svc@cosmo.cats", "Svc Cat");

        assertNotNull(created.getId());
        assertEquals("svc@cosmo.cats", customerService.findById(created.getId()).getEmail());
        assertEquals(1, customerService.findAll().size());

        customerService.delete(created.getId());

        assertEquals(0, customerService.findAll().size());
        assertThrows(CustomerNotFoundException.class, () -> customerService.findById(created.getId()));
        assertThrows(CustomerNotFoundException.class, () -> customerService.delete(UUID.randomUUID()));
    }

    @Test
    void customerServiceShouldRejectDuplicateEmail() {
        customerService.create("dup@cosmo.cats", "First");

        assertThrows(DataIntegrityViolationException.class,
                () -> customerService.create("dup@cosmo.cats", "Second"));
    }
}
