package com.vignesh.ssl.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Utility class for file storage operations (PEM/DER files and JSON metadata)
 */
public class FileStorageUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String STORAGE_BASE_PATH = "certificates-storage";

    private FileStorageUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Get the base storage path
     *
     * @return Path to certificates-storage directory
     */
    public static Path getStoragePath() {
        return Paths.get(STORAGE_BASE_PATH);
    }

    /**
     * Get a subdirectory path within storage
     *
     * @param subdirectory subdirectory name
     * @return Path to subdirectory
     */
    public static Path getSubdirectoryPath(String subdirectory) {
        return Paths.get(STORAGE_BASE_PATH, subdirectory);
    }

    /**
     * Ensure a directory exists, creating it if necessary
     *
     * @param path directory path
     * @return the path
     */
    public static Path ensureDirectoryExists(Path path) {
        try {
            Files.createDirectories(path);
            logger.debug("Ensured directory exists: {}", path);
        } catch (IOException e) {
            logger.error("Failed to create directory: {}", path, e);
            throw new RuntimeException("Failed to create directory: " + path, e);
        }
        return path;
    }

    /**
     * Write content to a file
     *
     * @param path file path
     * @param content file content
     */
    public static void writeFile(Path path, String content) {
        try {
            ensureDirectoryExists(path.getParent());
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            logger.debug("Written file: {}", path);
        } catch (IOException e) {
            logger.error("Failed to write file: {}", path, e);
            throw new RuntimeException("Failed to write file: " + path, e);
        }
    }

    /**
     * Read content from a file
     *
     * @param path file path
     * @return file content
     */
    public static String readFile(Path path) {
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            logger.debug("Read file: {}", path);
            return content;
        } catch (IOException e) {
            logger.error("Failed to read file: {}", path, e);
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    /**
     * Write metadata (JSON) to a file
     *
     * @param path file path
     * @param metadata metadata map
     */
    public static void writeMetadata(Path path, Map<String, Object> metadata) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata);
            writeFile(path, json);
            logger.debug("Written metadata: {}", path);
        } catch (IOException e) {
            logger.error("Failed to write metadata: {}", path, e);
            throw new RuntimeException("Failed to write metadata: " + path, e);
        }
    }

    /**
     * Read metadata (JSON) from a file
     *
     * @param path file path
     * @return metadata map
     */
    public static Map<String, Object> readMetadata(Path path) {
        try {
            String content = readFile(path);
            Map<String, Object> metadata = objectMapper.readValue(content, Map.class);
            logger.debug("Read metadata: {}", path);
            return metadata;
        } catch (IOException e) {
            logger.error("Failed to read metadata: {}", path, e);
            throw new RuntimeException("Failed to read metadata: " + path, e);
        }
    }

    /**
     * Delete a file
     *
     * @param path file path
     */
    public static void deleteFile(Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                logger.debug("Deleted file: {}", path);
            }
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", path, e);
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    /**
     * Check if a file exists
     *
     * @param path file path
     * @return true if file exists
     */
    public static boolean fileExists(Path path) {
        return Files.exists(path);
    }

    /**
     * Get the parent directory of a file
     *
     * @param path file path
     * @return parent directory path
     */
    public static Path getParentDirectory(Path path) {
        return path.getParent();
    }
}
