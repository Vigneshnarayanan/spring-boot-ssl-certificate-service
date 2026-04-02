package com.vignesh.ssl.util;

import java.util.UUID;

/**
 * Utility class for generating consistent UUIDs
 */
public class UuidGenerator {
    
    private UuidGenerator() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Generate a random UUID string without hyphens
     *
     * @return UUID string without hyphens
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a random UUID string with hyphens
     *
     * @return UUID string with hyphens
     */
    public static String generateWithHyphens() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a random UUID string with a prefix
     *
     * @param prefix prefix for the UUID
     * @return UUID string with prefix
     */
    public static String generateWithPrefix(String prefix) {
        return prefix + "-" + generate();
    }
}
