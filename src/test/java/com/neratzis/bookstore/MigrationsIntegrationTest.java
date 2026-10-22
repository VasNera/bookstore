package com.neratzis.bookstore;

import org.junit.jupiter.api.Test;

class MigrationsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void contextLoads_andAllFlywayMigrationsPassSchemaValidation() {
        // Spring context + Testcontainers MySQL + Flyway V1..V6 + ddl-auto=validate
    }
}
