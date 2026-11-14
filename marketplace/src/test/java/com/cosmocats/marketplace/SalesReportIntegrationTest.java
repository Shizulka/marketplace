package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.service.OrderService;
import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.application.service.SalesReportService;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CategoryEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSalesProjection;
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
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SalesReportIntegrationTest extends AbstractPostgresTest {

    @Autowired private SalesReportService reportService;
    @Autowired private OrderService orderService;
    @Autowired private CategoryJpaRepository categoryRepository;
    @Autowired private ProductJpaRepository productRepository;
    @Autowired private CustomerJpaRepository customerRepository;

    private ProductEntity yarn;
    private ProductEntity milk;
    private ProductEntity comet;

    @BeforeEach
    void setUp() {
        CategoryEntity category = categoryRepository.save(new CategoryEntity("Toys", null));
        yarn = productRepository.save(new ProductEntity("Yarn", "d", BigDecimal.TEN, category, 100));
        milk = productRepository.save(new ProductEntity("Milk", "d", BigDecimal.ONE, category, 100));
        comet = productRepository.save(new ProductEntity("Comet", "d", BigDecimal.TEN, category, 100));
        CustomerEntity customer = customerRepository.save(new CustomerEntity("c@cosmo.cats", "Cat"));

        orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 2), new OrderLine(milk.getId(), 5)));
        orderService.create(customer.getId(), List.of(new OrderLine(yarn.getId(), 4), new OrderLine(comet.getId(), 5)));
        OrderEntity cancelled = orderService.create(customer.getId(), List.of(new OrderLine(comet.getId(), 50)));
        orderService.updateStatus(cancelled.getId(), OrderStatus.CANCELLED);
    }

    @Test
    void topProductsShouldAggregateSortByQuantityThenNameAndSkipCancelled() {
        List<ProductSalesProjection> report = reportService.topProducts(10, null);

        assertEquals(3, report.size());
        assertEquals("Yarn", report.get(0).getProductName());
        assertEquals(6L, report.get(0).getTotalQuantity());
        assertEquals(yarn.getId(), report.get(0).getProductId());
        // Comet (5) і Milk (5): однакова кількість, сортування за назвою
        assertEquals("Comet", report.get(1).getProductName());
        assertEquals(5L, report.get(1).getTotalQuantity());
        assertEquals("Milk", report.get(2).getProductName());
    }

    @Test
    void topProductsShouldRespectLimit() {
        List<ProductSalesProjection> report = reportService.topProducts(1, Instant.EPOCH);

        assertEquals(1, report.size());
        assertEquals("Yarn", report.get(0).getProductName());
    }

    @Test
    void topProductsShouldFilterBySinceDate() {
        assertTrue(reportService.topProducts(10, Instant.now().plusSeconds(3600)).isEmpty());
    }
}
