package com.college.cashflow.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EnvLoaderTest {

    private String originalPassword;
    private String originalSecret;

    @BeforeEach
    void setUp() {
        originalPassword = System.getProperty("DB_PASSWORD");
        originalSecret = System.getProperty("JWT_SECRET");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("JWT_SECRET");
        EnvLoader.resetForTesting();
    }

    @AfterEach
    void tearDown() {
        if (originalPassword != null) {
            System.setProperty("DB_PASSWORD", originalPassword);
        } else {
            System.clearProperty("DB_PASSWORD");
        }

        if (originalSecret != null) {
            System.setProperty("JWT_SECRET", originalSecret);
        } else {
            System.clearProperty("JWT_SECRET");
        }
        EnvLoader.resetForTesting();
    }

    @Test
    void testLoadFileParsesPropertiesAndStripsQuotes(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # Sample Comment
                PORT=8081
                DB_USERNAME='local_admin'
                DB_PASSWORD="secure_db_pass"
                JWT_SECRET=super_secret_jwt_key_that_is_at_least_32_characters_long
                """);

        EnvLoader.loadFile(envFile);

        assertEquals("local_admin", System.getProperty("DB_USERNAME"));
        assertEquals("secure_db_pass", System.getProperty("DB_PASSWORD"));
        assertEquals("super_secret_jwt_key_that_is_at_least_32_characters_long", System.getProperty("JWT_SECRET"));
        assertEquals("8081", System.getProperty("PORT"));

        // Clean up set properties
        System.clearProperty("DB_USERNAME");
        System.clearProperty("PORT");
    }

    @Test
    void testValidateFailsWhenDbPasswordMissing() {
        System.setProperty("JWT_SECRET", "super_secret_jwt_key_that_is_at_least_32_characters_long");

        IllegalStateException ex = assertThrows(IllegalStateException.class, EnvLoader::validate);
        assertTrue(ex.getMessage().contains("DB_PASSWORD"));
    }

    @Test
    void testValidateFailsWhenJwtSecretMissing() {
        System.setProperty("DB_PASSWORD", "mypassword");

        IllegalStateException ex = assertThrows(IllegalStateException.class, EnvLoader::validate);
        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void testValidateFailsWhenJwtSecretTooShort() {
        System.setProperty("DB_PASSWORD", "mypassword");
        System.setProperty("JWT_SECRET", "short_secret");

        IllegalStateException ex = assertThrows(IllegalStateException.class, EnvLoader::validate);
        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void testValidateSucceedsWhenValid() {
        System.setProperty("DB_PASSWORD", "mypassword");
        System.setProperty("JWT_SECRET", "super_secret_jwt_key_that_is_at_least_32_characters_long");

        assertDoesNotThrow(EnvLoader::validate);
    }
}
