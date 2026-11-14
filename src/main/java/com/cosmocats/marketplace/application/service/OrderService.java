package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.application.exception.CustomerNotFoundException;
import com.cosmocats.marketplace.application.exception.InsufficientStockException;
import com.cosmocats.marketplace.application.exception.OrderNotFoundException;
import com.cosmocats.marketplace.application.exception.ProductNotFoundException;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.CustomerEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderItemEntity;
import com.cosmocats.marketplace.infrastructure.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.infrastructure.persistence.repository.CustomerJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.OrderJpaRepository;
import com.cosmocats.marketplace.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    public record OrderLine(UUID productId, int quantity) {
    }

    private final OrderJpaRepository orderRepository;
    private final CustomerJpaRepository customerRepository;
    private final ProductJpaRepository productRepository;

    public OrderService(OrderJpaRepository orderRepository,
                        CustomerJpaRepository customerRepository,
                        ProductJpaRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    /**
     * Створення замовлення — одна транзакція: перевірка залишків, списання зі складу
     * та збереження позицій. Якщо хоч одна позиція не проходить, відкочується все.
     */
    public OrderEntity create(UUID customerId, List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        Map<UUID, Integer> quantities = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            if (line == null || line.productId() == null || line.quantity() <= 0) {
                throw new IllegalArgumentException("Each order item must have a product and a positive quantity.");
            }
            quantities.merge(line.productId(), line.quantity(), Math::addExact);
        }

        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        OrderEntity order = new OrderEntity(generateOrderNumber(), customer);

        quantities.forEach((productId, quantity) -> {
            ProductEntity product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            if (product.getStockQuantity() < quantity) {
                throw new InsufficientStockException(productId, product.getStockQuantity(), quantity);
            }
            product.setStockQuantity(product.getStockQuantity() - quantity);
            order.addItem(new OrderItemEntity(product, quantity, product.getPrice()));
        });

        return orderRepository.saveAndFlush(order);
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findAll() {
        return orderRepository.findAll(Sort.by("createdAt", "id"));
    }

    @Transactional(readOnly = true)
    public OrderEntity findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    /** Пошук за Natural ID замість surrogate UUID. */
    @Transactional(readOnly = true)
    public OrderEntity findByOrderNumber(String orderNumber) {
        return orderRepository.loadByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public OrderEntity updateStatus(UUID id, OrderStatus newStatus) {
        OrderEntity order = findById(id);
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status is required.");
        }
        if (!isAllowedTransition(order.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Cannot change order status from " + order.getStatus() + " to " + newStatus + ".");
        }
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItemEntity item : order.getItems()) {
                ProductEntity product = productRepository.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProduct().getId()));
                product.setStockQuantity(Math.addExact(product.getStockQuantity(), item.getQuantity()));
            }
        }
        order.setStatus(newStatus);
        return orderRepository.saveAndFlush(order);
    }

    public void delete(UUID id) {
        OrderEntity order = findById(id);
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Shipped or delivered orders cannot be deleted.");
        }
        // NEW/PAID orders reserve stock, so deleting them must release that reservation.
        // CANCELLED orders have already restored stock and must not restore it twice.
        if (order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItemEntity item : order.getItems()) {
                UUID productId = item.getProduct().getId();
                ProductEntity product = productRepository.findByIdForUpdate(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));
                product.setStockQuantity(Math.addExact(product.getStockQuantity(), item.getQuantity()));
            }
        }
        orderRepository.delete(order);
        orderRepository.flush();
    }

    private static boolean isAllowedTransition(OrderStatus current, OrderStatus target) {
        return switch (current) {
            case NEW -> target == OrderStatus.PAID || target == OrderStatus.CANCELLED;
            case PAID -> target == OrderStatus.SHIPPED || target == OrderStatus.CANCELLED;
            case SHIPPED -> target == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private static String generateOrderNumber() {
        return "CC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
