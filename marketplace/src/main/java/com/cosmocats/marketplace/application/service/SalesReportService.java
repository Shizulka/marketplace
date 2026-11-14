package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.domain.model.OrderStatus;
import com.cosmocats.marketplace.infrastructure.persistence.projection.ProductSalesProjection;
import com.cosmocats.marketplace.infrastructure.persistence.repository.OrderItemJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SalesReportService {

    private final OrderItemJpaRepository orderItemRepository;

    public SalesReportService(OrderItemJpaRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    /** Найчастіше купувані товари (скасовані замовлення не враховуються). */
    public List<ProductSalesProjection> topProducts(int limit, Instant since) {
        Instant from = since != null ? since : Instant.EPOCH;
        return orderItemRepository.findTopSoldProducts(
                OrderStatus.CANCELLED, from, PageRequest.of(0, limit));
    }
}
