package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.KeystoreResponse;
import com.vignesh.ssl.util.FileStorageUtil;
import com.vignesh.ssl.util.KeystoreUtil;
import com.vignesh.ssl.util.UuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing keystores
 */
@Service
public class KeystoreService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeystoreService.class);
    
    private final CertificateService certificateService;

    public KeystoreService(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    /**
     * Generate a new keystore from a certificate
     *
     * @param certificateId certificate ID
     * @param keystoreType PKCS12 or JKS (only PKCS12 supported)
     * @param password keystore password
     * @param alias certificate alias
     * @return keystore response
     * @throws CertificateException if keystore generation fails
     */
    public KeystoreResponse generateKeystore(String certificateId, String keystoreType,
                                            String password, String alias) throws CertificateException {
        try {
            validateKeystoreRequest(password, alias, keystoreType);
            
            String keystoreId = UuidGenerator.generate();
            logger.info("Generating keystore {} from certificate {}", keystoreId, certificateId);
            
            X509Certificate certificate = certificateService.getCertificateAsX509(certificateId);
            
            Path privateKeyPath = java.nio.file.Paths.get(
                FileStorageUtil.getSubdirectoryPath("certificates").toString(),
                certificateId + ".key");
            
            if (!Files.exists(privateKeyPath)) {
                throw new CertificateException("Private key not found for certificate: " + certificateId);
            }
            
            String privateKeyPem = FileStorageUtil.readFile(privateKeyPath);
            java.security.PrivateKey privateKey = parsePrivateKeyPem(privateKeyPem);
            
            KeyStore keystore = KeystoreUtil.createKeystore(certificate, privateKey, password, alias);
            byte[] keystoreBytes = KeystoreUtil.exportKeystore(keystore, password);
            KeystoreUtil.saveKeystore(keystoreId, keystoreBytes, password);
            
            saveKeystoreMetadata(keystoreId, certificateId, keystoreType, alias, certificate);
            
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                certificate.getNotAfter().toInstant(), ZoneId.systemDefault());
            
            KeystoreResponse response = new KeystoreResponse(
                keystoreId,
                certificateId,
                keystoreType,
                alias,
                certificate.getSubjectDN().getName(),
                LocalDateTime.now(),
                expiresAt
            );
            
            logger.info("Successfully generated keystore: {}", keystoreId);
            return response;
        } catch (ValidationException e) {
            throw new CertificateException(e.getMessage());
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to generate keystore", e);
            throw new CertificateException("Failed to generate keystore", e);
        }
    }

    /**
     * Get a keystore by ID
     *
     * @param keystoreId keystore ID
     * @return keystore response
     * @throws CertificateException if keystore not found
     */
    public KeystoreResponse getKeystore(String keystoreId) throws CertificateException {
        try {
            Path metadataPath = KeystoreUtil.getKeystoreMetadataPath(keystoreId);
            if (!Files.exists(metadataPath)) {
                throw new CertificateException("Keystore not found: " + keystoreId);
            }
            
            Map<String, Object> metadata = FileStorageUtil.readMetadata(metadataPath);
            
            KeystoreResponse response = new KeystoreResponse(
                keystoreId,
                (String) metadata.get("certificateId"),
                (String) metadata.get("keystoreType"),
                (String) metadata.get("alias"),
                (String) metadata.get("commonName"),
                LocalDateTime.parse((String) metadata.get("createdAt")),
                LocalDateTime.parse((String) metadata.get("expiresAt"))
            );
            response.setStatus((String) metadata.get("status"));
            
            logger.debug("Retrieved keystore: {}", keystoreId);
            return response;
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get keystore", e);
            throw new CertificateException("Failed to get keystore", e);
        }
    }

    /**
     * Export a keystore as bytes
     *
     * @param keystoreId keystore ID
     * @return keystore bytes
     * @throws CertificateException if export fails
     */
    public byte[] exportKeystore(String keystoreId) throws CertificateException {
        try {
            Path keystorePath = KeystoreUtil.getKeystorePath(keystoreId);
            if (!Files.exists(keystorePath)) {
                throw new CertificateException("Keystore not found: " + keystoreId);
            }
            
            byte[] keystoreBytes = Files.readAllBytes(keystorePath);
            logger.debug("Exported keystore: {}", keystoreId);
            return keystoreBytes;
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to export keystore", e);
            throw new CertificateException("Failed to export keystore", e);
        }
    }

    /**
     * List all keystores
     *
     * @return list of keystore responses
     */
    public List<KeystoreResponse> listKeystores() {
        try {
            Path keystoresDir = FileStorageUtil.getSubdirectoryPath("keystores");
            if (!Files.exists(keystoresDir)) {
                return new ArrayList<>();
            }
            
            List<KeystoreResponse> keystores = Files.list(keystoresDir)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .map(metadataPath -> {
                    try {
                        String keystoreId = metadataPath.getFileName().toString()
                            .replace(".json", "");
                        return getKeystore(keystoreId);
                    } catch (Exception e) {
                        logger.warn("Failed to load keystore", e);
                        return null;
                    }
                })
                .filter(ks -> ks != null)
                .collect(Collectors.toList());
            
            logger.debug("Listed {} keystores", keystores.size());
            return keystores;
        } catch (Exception e) {
            logger.error("Failed to list keystores", e);
            return new ArrayList<>();
        }
    }

    /**
     * Save keystore metadata
     *
     * @param keystoreId keystore ID
     * @param certificateId certificate ID
     * @param keystoreType keystore type
     * @param alias certificate alias
     * @param certificate X509Certificate
     */
    private void saveKeystoreMetadata(String keystoreId, String certificateId,
                                     String keystoreType, String alias, X509Certificate certificate) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", keystoreId);
        metadata.put("type", "KEYSTORE");
        metadata.put("certificateId", certificateId);
        metadata.put("keystoreType", keystoreType);
        metadata.put("alias", alias);
        metadata.put("commonName", certificate.getSubjectDN().getName());
        metadata.put("createdAt", LocalDateTime.now().toString());
        metadata.put("expiresAt", LocalDateTime.ofInstant(
            certificate.getNotAfter().toInstant(), ZoneId.systemDefault()).toString());
        metadata.put("status", "active");
        
        FileStorageUtil.writeMetadata(KeystoreUtil.getKeystoreMetadataPath(keystoreId), metadata);
        logger.debug("Saved keystore metadata: {}", keystoreId);
    }

    /**
     * Validate keystore request
     *
     * @param password keystore password
     * @param alias certificate alias
     * @param keystoreType keystore type
     */
    private void validateKeystoreRequest(String password, String alias, String keystoreType) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Keystore password is required");
        }
        if (alias == null || alias.isEmpty()) {
            throw new ValidationException("Certificate alias is required");
        }
        if (keystoreType == null || keystoreType.isEmpty()) {
            throw new ValidationException("Keystore type is required");
        }
        if (!keystoreType.equalsIgnoreCase("PKCS12") && !keystoreType.equalsIgnoreCase("JKS")) {
            throw new ValidationException("Unsupported keystore type: " + keystoreType);
        }
    }

    /**
     * Parse private key from PEM format
     *
     * @param privateKeyPem private key in PEM format
     * @return PrivateKey
     */
    private java.security.PrivateKey parsePrivateKeyPem(String privateKeyPem) throws Exception {
        try {
            org.bouncycastle.openssl.PEMParser pemParser =
                new org.bouncycastle.openssl.PEMParser(new java.io.StringReader(privateKeyPem));
            Object keyObject = pemParser.readObject();
            pemParser.close();
            
            if (keyObject instanceof org.bouncycastle.openssl.PEMKeyPair) {
                org.bouncycastle.openssl.PEMKeyPair keyPair =
                    (org.bouncycastle.openssl.PEMKeyPair) keyObject;
                org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter converter =
                    new org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter();
                return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            }
            
            throw new Exception("Invalid private key format");
        } catch (Exception e) {
            logger.error("Failed to parse private key", e);
            throw e;
        }
    }
}
