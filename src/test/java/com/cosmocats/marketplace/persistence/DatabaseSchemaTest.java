package com.cosmocats.marketplace.persistence;

import com.cosmocats.marketplace.support.AbstractPostgresTest;
import com.cosmocats.marketplace.support.PostgresDataJpaTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Перевіряє, що Liquibase-міграції справді створили заявлену схему в PostgreSQL. */
@PostgresDataJpaTest
class DatabaseSchemaTest extends AbstractPostgresTest {

    @Autowired private JdbcTemplate jdbc;

    private boolean exists(String sql, String name) {
        Integer count = jdbc.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    @Test
    void tablesShouldExist() {
        for (String table : List.of("categories", "products", "customers", "orders", "order_items")) {
            assertTrue(exists("SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema = 'public' AND table_name = ?", table), table);
        }
    }

    @Test
    void sequencesShouldExist() {
        for (String sequence : List.of("categories_seq", "order_items_seq")) {
            assertTrue(exists("SELECT COUNT(*) FROM pg_sequences WHERE sequencename = ?", sequence), sequence);
        }
    }

    @Test
    void indexesFromMigrationsShouldExist() {
        for (String index : List.of("idx_products_name", "idx_orders_customer_id",
                "idx_order_items_product_id", "idx_orders_status")) {
            assertTrue(exists("SELECT COUNT(*) FROM pg_indexes "
                    + "WHERE schemaname = 'public' AND indexname = ?", index), index);
        }
    }

    @Test
    void redundantIndexShouldBeDroppedByMigration() {
        assertFalse(exists("SELECT COUNT(*) FROM pg_indexes WHERE indexname = ?", "idx_products_category_id"));
    }

    @Test
    void uniqueConstraintsFromMigrationsShouldExist() {
        for (String constraint : List.of("uq_categories_name", "uq_products_category_name",
                "uq_customers_email", "uq_order_items_order_product", "uq_orders_order_number")) {
            assertTrue(exists("SELECT COUNT(*) FROM information_schema.table_constraints "
                    + "WHERE constraint_type = 'UNIQUE' AND constraint_name = ?", constraint), constraint);
        }
    }

    @Test
    void changelogShouldBeVersionedAndDemoDataSkippedInTestContext() {
        for (String prefix : List.of("1.0-%", "1.1-%", "1.2-%", "1.3-%")) {
            assertTrue(exists("SELECT COUNT(*) FROM databasechangelog WHERE id LIKE ?", prefix), prefix);
        }
        assertFalse(exists("SELECT COUNT(*) FROM databasechangelog WHERE id LIKE ?", "1.4-%"));
    }

    @Test
    void orderStatusCheckConstraintShouldRejectUnknownStatus() {
        jdbc.update("INSERT INTO customers (email, full_name) VALUES ('schema@cosmo.cats', 'Schema Cat')");

        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO orders (customer_id, order_number, status) "
                        + "SELECT id, 'CC-BOGUS', 'BOGUS' FROM customers WHERE email = 'schema@cosmo.cats'"));
    }
}
