package com.cosmocats.marketplace.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.lang.annotation.*;

/**
 * Slice-тест: піднімається лише JPA-частина контексту (сутності, Spring Data репозиторії, Liquibase),
 * але на справжній PostgreSQL з Testcontainers, а не на вбудованій H2.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public @interface PostgresDataJpaTest {
}
