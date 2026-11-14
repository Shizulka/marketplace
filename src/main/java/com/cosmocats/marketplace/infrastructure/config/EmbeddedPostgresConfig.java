package com.cosmocats.marketplace.infrastructure.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Профіль "local" (активний за замовчуванням): додаток сам запускає вбудований PostgreSQL,
 * без Docker і без ручного створення бази. Дані зберігаються в каталозі .local-postgres,
 * тому між запусками не губляться. Liquibase застосовує міграції на цю ж базу.
 * Для справжнього сервера: --spring.profiles.active=prod + spring.datasource.* / DB_* змінні.
 */
@Configuration
@Profile("local")
public class EmbeddedPostgresConfig {

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.builder()
                .setDataDirectory(Path.of(".local-postgres"))
                .setCleanDataDirectory(false)
                .start();
    }

    @Bean
    public DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
        return embeddedPostgres.getPostgresDatabase();
    }
}