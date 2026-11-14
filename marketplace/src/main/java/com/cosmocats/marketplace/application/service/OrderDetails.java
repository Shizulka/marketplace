package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Плоский знімок замовлення, зібраний усередині транзакції (відповідь REST без lazy-проксі). */
public record OrderDetails(
        UUID id,
        String orderNumber,
        UUID customerId,
        OrderStatus status,
        Instant createdAt,
        BigDecimal totalPrice,
        List<Item> items) {

    public record Item(UUID productId, String productName, Integer quantity, BigDecimal unitPrice) {
    }

    public static OrderDetails from(OrderEntity order) {
        List<Item> items = order.getItems().stream()
                .map(i -> new Item(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice()))
                .toList();
        return new OrderDetails(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                items);
    }
}
