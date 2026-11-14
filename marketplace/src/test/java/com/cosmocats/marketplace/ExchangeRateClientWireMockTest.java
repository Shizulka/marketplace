package com.cosmocats.marketplace;

import com.cosmocats.marketplace.application.exception.CurrencyNotSupportedException;
import com.cosmocats.marketplace.application.exception.ExchangeRateUnavailableException;
import com.cosmocats.marketplace.infrastructure.client.ExchangeRateClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateClientWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ExchangeRateClient client() {
        return new ExchangeRateClient(wireMock.baseUrl());
    }

    @Test
    void getRateShouldReturnRateFromExternalService() {
        wireMock.stubFor(get(urlPathEqualTo("/api/rates"))
                .withQueryParam("base", equalTo("USD"))
                .withQueryParam("target", equalTo("EUR"))
                .willReturn(okJson("""
                        {"base":"USD","target":"EUR","rate":0.92}
                        """)));

        BigDecimal rate = client().getRate("USD", "EUR");

        assertEquals(0, new BigDecimal("0.92").compareTo(rate));
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/api/rates")));
    }

    @Test
    void getRateShouldThrowCurrencyNotSupportedOn404() {
        wireMock.stubFor(get(urlPathEqualTo("/api/rates"))
                .withQueryParam("target", equalTo("XXX"))
                .willReturn(notFound()));

        assertThrows(CurrencyNotSupportedException.class,
                () -> client().getRate("USD", "XXX"));
    }

    @Test
    void getRateShouldThrowUnavailableOnServerError() {
        wireMock.stubFor(get(urlPathEqualTo("/api/rates")).willReturn(serverError()));

        assertThrows(ExchangeRateUnavailableException.class,
                () -> client().getRate("USD", "EUR"));
    }

    @Test
    void getRateShouldThrowUnavailableOnConnectionReset() {
        wireMock.stubFor(get(urlPathEqualTo("/api/rates"))
                .willReturn(aResponse().withFault(
                        com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

        assertThrows(ExchangeRateUnavailableException.class,
                () -> client().getRate("USD", "EUR"));
    }

    @Test
    void getRateShouldThrowUnavailableOnEmptyBody() {
        wireMock.stubFor(get(urlPathEqualTo("/api/rates")).willReturn(okJson("{}")));

        assertThrows(ExchangeRateUnavailableException.class,
                () -> client().getRate("USD", "EUR"));
    }
}