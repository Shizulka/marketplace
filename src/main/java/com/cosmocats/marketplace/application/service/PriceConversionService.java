package com.cosmocats.marketplace.application.service;

import com.cosmocats.marketplace.domain.model.Product;
import com.cosmocats.marketplace.infrastructure.client.ExchangeRateClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PriceConversionService {

    public static final String BASE_CURRENCY = "USD";

    private final ProductService productService;
    private final ExchangeRateClient exchangeRateClient;

    public PriceConversionService(ProductService productService,
                                  ExchangeRateClient exchangeRateClient) {
        this.productService = productService;
        this.exchangeRateClient = exchangeRateClient;
    }

    public BigDecimal convertPrice(UUID productId, String currency) {
        Product product = productService.findById(productId);
        String target = currency.toUpperCase();

        if (BASE_CURRENCY.equals(target)) {
            return product.getPrice();
        }

        BigDecimal rate = exchangeRateClient.getRate(BASE_CURRENCY, target);
        return product.getPrice().multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}