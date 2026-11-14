package com.cosmocats.marketplace.presentation.controller;

import com.cosmocats.marketplace.application.service.OrderDetails;
import com.cosmocats.marketplace.application.service.OrderFacade;
import com.cosmocats.marketplace.application.service.OrderService.OrderLine;
import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.presentation.dto.OrderRequest;
import com.cosmocats.marketplace.presentation.dto.OrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @PostMapping
    public ResponseEntity<OrderDetails> create(@Valid @RequestBody OrderRequest request) {
        List<OrderLine> lines = request.items().stream()
                .map(i -> new OrderLine(i.productId(), i.quantity()))
                .toList();
        OrderDetails created = orderFacade.create(request.customerId(), lines);
        return ResponseEntity
                .created(URI.create("/api/orders/" + created.id()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<OrderDetails>> findAll(
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(status == null
                ? orderFacade.findAll()
                : orderFacade.findByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetails> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderFacade.findById(id));
    }

    /** Пошук за Natural ID (номер замовлення), без UUID. */
    @GetMapping("/by-number/{orderNumber}")
    public ResponseEntity<OrderDetails> findByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderFacade.findByOrderNumber(orderNumber));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDetails> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderFacade.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderFacade.delete(id);
        return ResponseEntity.noContent().build();
    }
}
