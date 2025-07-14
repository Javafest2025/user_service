package org.solace.scholar_ai.user_service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testFlywayMigration() {
        // Verify that Flyway has run and created the schema
        assertNotNull(dataSource);

        // The test will fail if Flyway migrations don't run successfully
        // This is a simple smoke test to ensure migrations work
    }
}
