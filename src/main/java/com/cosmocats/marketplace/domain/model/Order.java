package com.cosmocats.marketplace.domain.model;

import java.math.BigDecimal;
import java.util.List;

public class Order {

    private Long id;
    private Long customerId;
    private List<Long> productIds;
    private BigDecimal totalPrice;
    private String status;

    public Order() {
    }

    public Order(Long id, Long customerId, List<Long> productIds, BigDecimal totalPrice, String status) {
        this.id = id;
        this.customerId = customerId;
        this.productIds = productIds;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}