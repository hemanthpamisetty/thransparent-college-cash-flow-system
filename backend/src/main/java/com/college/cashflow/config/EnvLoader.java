package com.college.cashflow.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight local environment loader and configuration validator.
 * <p>
 * Loads configuration from a local .env file (if present) into System properties,
 * without overriding already-defined OS environment variables or System properties.
 * Validates required configuration before Spring Boot components initialize.
 * Never logs sensitive credential values.
 */
public final class EnvLoader {

    private static final List<Path> CANDIDATE_PATHS = List.of(
            Path.of(".env"),
            Path.of("backend", ".env"),
            Path.of("..", ".env"),
            Path.of("..", "backend", ".env")
    );

    private static boolean loaded = false;

    private EnvLoader() {
        // Utility class
    }

    /**
     * Loads .env if not already loaded.
     */
    public static synchronized void load() {
        if (loaded) {
            return;
        }

        for (Path candidate : CANDIDATE_PATHS) {
            if (Files.isRegularFile(candidate)) {
                loadFile(candidate);
                break;
            }
        }
        loaded = true;
    }

    /**
     * Loads .env (if present) and validates required configuration.
     * Throws IllegalStateException if any required variable is missing or invalid.
     */
    public static synchronized void loadAndValidate() {
        load();
        validate();
    }

    /**
     * Validates that required environment variables are present and meet security standards.
     * Does NOT log secret values.
     */
    public static void validate() {
        List<String> errors = new ArrayList<>();

        String dbPassword = get("DB_PASSWORD");
        if (dbPassword == null || dbPassword.trim().isEmpty()) {
            errors.add("DB_PASSWORD is missing. Set DB_PASSWORD in backend/.env or as an environment variable.");
        }

        String jwtSecret = get("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            errors.add("JWT_SECRET is missing. Set a 256-bit (>= 32 characters) key in backend/.env or as an environment variable.");
        } else if (jwtSecret.trim().length() < 32) {
            errors.add("JWT_SECRET is too short (" + jwtSecret.trim().length() + " chars). HMAC-SHA256 requires at least 32 characters (256 bits).");
        }

        if (!errors.isEmpty()) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println(" CRITICAL CONFIGURATION ERROR: Missing or Invalid Configuration");
            System.err.println("=".repeat(80));
            for (String err : errors) {
                System.err.println("  * " + err);
            }
            System.err.println("\n Quick Fix for Developers:");
            System.err.println("  1. Copy backend/.env.example to backend/.env: Copy-Item .env.example .env");
            System.err.println("  2. Fill in your local DB_PASSWORD and a secure JWT_SECRET (32+ characters)");
            System.err.println("  3. Never commit backend/.env to Git");
            System.err.println("=".repeat(80) + "\n");
            throw new IllegalStateException("Application startup aborted: missing required configuration (DB_PASSWORD / JWT_SECRET).");
        }
    }

    /**
     * Retrieves configuration value from System properties first, then System environment.
     */
    public static String get(String key) {
        String prop = System.getProperty(key);
        if (prop != null && !prop.isEmpty()) {
            return prop;
        }
        return System.getenv(key);
    }

    /**
     * Parses a .env file and sets non-existing System properties.
     */
    static void loadFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int equalsIndex = line.indexOf('=');
                if (equalsIndex <= 0) {
                    continue;
                }
                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();

                // Strip surrounding quotes if present
                if (value.length() >= 2 &&
                        ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }

                // Only set if not already present in OS environment or System properties
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Notice: Could not read .env from " + path + ": " + e.getMessage());
        }
    }

    /**
     * Resets loaded state (used for testing).
     */
    static synchronized void resetForTesting() {
        loaded = false;
    }
}
