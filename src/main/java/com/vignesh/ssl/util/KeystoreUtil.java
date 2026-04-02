package com.vignesh.ssl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * Utility class for PKCS12 keystore operations
 */
public class KeystoreUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(KeystoreUtil.class);
    private static final String KEYSTORES_DIRECTORY = "keystores";

    private KeystoreUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Create a new PKCS12 keystore with a certificate and private key
     *
     * @param certificate X509Certificate
     * @param privateKey private key
     * @param password keystore password
     * @param alias certificate alias
     * @return KeyStore
     * @throws Exception if keystore creation fails
     */
    public static KeyStore createKeystore(X509Certificate certificate, PrivateKey privateKey,
                                         String password, String alias) throws Exception {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(null, password.toCharArray());
            
            Certificate[] chain = new Certificate[]{certificate};
            keystore.setKeyEntry(alias, privateKey, password.toCharArray(), chain);
            
            logger.debug("Created PKCS12 keystore with alias: {}", alias);
            return keystore;
        } catch (Exception e) {
            logger.error("Failed to create keystore", e);
            throw new Exception("Failed to create keystore", e);
        }
    }

    /**
     * Export keystore to byte array
     *
     * @param keystore KeyStore
     * @param password keystore password
     * @return byte array representation of keystore
     * @throws Exception if export fails
     */
    public static byte[] exportKeystore(KeyStore keystore, String password) throws Exception {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            keystore.store(baos, password.toCharArray());
            byte[] keystoreBytes = baos.toByteArray();
            logger.debug("Exported keystore to byte array, size: {} bytes", keystoreBytes.length);
            return keystoreBytes;
        } catch (Exception e) {
            logger.error("Failed to export keystore", e);
            throw new Exception("Failed to export keystore", e);
        }
    }

    /**
     * Get the path for a keystore file
     *
     * @param keystoreId keystore ID
     * @return Path to keystore file
     */
    public static Path getKeystorePath(String keystoreId) {
        return Paths.get(FileStorageUtil.getSubdirectoryPath(KEYSTORES_DIRECTORY).toString(),
                keystoreId + ".p12");
    }

    /**
     * Get the path for keystore metadata
     *
     * @param keystoreId keystore ID
     * @return Path to metadata file
     */
    public static Path getKeystoreMetadataPath(String keystoreId) {
        return Paths.get(FileStorageUtil.getSubdirectoryPath(KEYSTORES_DIRECTORY).toString(),
                keystoreId + ".json");
    }

    /**
     * Save keystore to file
     *
     * @param keystoreId keystore ID
     * @param keystoreBytes keystore bytes
     * @param password keystore password
     * @throws Exception if save fails
     */
    public static void saveKeystore(String keystoreId, byte[] keystoreBytes, String password) throws Exception {
        try {
            Path path = getKeystorePath(keystoreId);
            FileStorageUtil.ensureDirectoryExists(path.getParent());
            java.nio.file.Files.write(path, keystoreBytes);
            logger.debug("Saved keystore: {}", keystoreId);
        } catch (IOException e) {
            logger.error("Failed to save keystore", e);
            throw new Exception("Failed to save keystore", e);
        }
    }

    /**
     * Load keystore from file
     *
     * @param keystoreId keystore ID
     * @param password keystore password
     * @return KeyStore
     * @throws Exception if load fails
     */
    public static KeyStore loadKeystore(String keystoreId, String password) throws Exception {
        try {
            Path path = getKeystorePath(keystoreId);
            byte[] keystoreBytes = java.nio.file.Files.readAllBytes(path);
            
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new java.io.ByteArrayInputStream(keystoreBytes), password.toCharArray());
            
            logger.debug("Loaded keystore: {}", keystoreId);
            return keystore;
        } catch (Exception e) {
            logger.error("Failed to load keystore", e);
            throw new Exception("Failed to load keystore", e);
        }
    }
}
