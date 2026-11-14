package com.cosmocats.marketplace.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Один спільний контейнер PostgreSQL на весь запуск тестів (singleton-патерн):
 * стартує при першому використанні, зупиняється Ryuk-ом після завершення JVM.
 * Liquibase застосовує справжні міграції, Hibernate перевіряє схему (ddl-auto=validate).
 */
public abstract class AbstractPostgresTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("marketplace")
            .withUsername("marketplace")
            .withPassword("marketplace");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName);
    }
}
