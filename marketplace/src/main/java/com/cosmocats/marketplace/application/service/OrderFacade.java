package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Обгортка над {@link OrderService} для REST-шару: вся робота й мапінг у {@link OrderDetails}
 * відбуваються в одній транзакції, тому lazy-зв'язки (позиції, товари, покупець) читаються безпечно
 * (open-in-view вимкнено).
 */
@Service
@Transactional
public class OrderFacade {

    private final OrderService orderService;

    public OrderFacade(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderDetails create(UUID customerId, List<OrderLine> lines) {
        return OrderDetails.from(orderService.create(customerId, lines));
    }

    @Transactional(readOnly = true)
    public List<OrderDetails> findAll() {
        return orderService.findAll().stream().map(OrderDetails::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDetails> findByStatus(OrderStatus status) {
        return orderService.findByStatus(status).stream().map(OrderDetails::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderDetails findById(UUID id) {
        return OrderDetails.from(orderService.findById(id));
    }

    @Transactional(readOnly = true)
    public OrderDetails findByOrderNumber(String orderNumber) {
        return OrderDetails.from(orderService.findByOrderNumber(orderNumber));
    }

    public OrderDetails updateStatus(UUID id, OrderStatus status) {
        return OrderDetails.from(orderService.updateStatus(id, status));
    }

    public void delete(UUID id) {
        orderService.delete(id);
    }
}
