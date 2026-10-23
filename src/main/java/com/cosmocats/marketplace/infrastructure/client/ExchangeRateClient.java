package com.cosmocats.marketplace.infrastructure.client;

import com.cosmocats.marketplace.application.exception.CurrencyNotSupportedException;
import com.cosmocats.marketplace.application.exception.ExchangeRateUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class ExchangeRateClient {

    public record RateResponse(String base, String target, BigDecimal rate) {
    }

    private final RestClient restClient;

    public ExchangeRateClient(@Value("${exchange-rate.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public BigDecimal getRate(String base, String target) {
        try {
            RateResponse response = restClient.get()
                    .uri("/api/rates?base={base}&target={target}", base, target)
                    .retrieve()
                    .body(RateResponse.class);

            if (response == null || response.rate() == null) {
                throw new ExchangeRateUnavailableException(
                        "Exchange rate service returned an empty response", null);
            }
            return response.rate();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new CurrencyNotSupportedException(target);
            }
            throw new ExchangeRateUnavailableException(
                    "Exchange rate service responded with " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new ExchangeRateUnavailableException(
                    "Exchange rate service is unreachable", e);
        }
    }
}