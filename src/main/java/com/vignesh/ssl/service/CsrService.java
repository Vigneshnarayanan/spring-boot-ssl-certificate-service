package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.CsrRequest;
import com.vignesh.ssl.model.CsrResponse;
import com.vignesh.ssl.util.CertificateUtil;
import com.vignesh.ssl.util.FileStorageUtil;
import com.vignesh.ssl.util.UuidGenerator;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing Certificate Signing Requests (CSRs)
 */
@Service
public class CsrService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsrService.class);
    private static final String CSRS_DIRECTORY = "csrs";
    private static final String CSR_SUFFIX = ".csr";
    private static final String METADATA_SUFFIX = ".json";
    private static final String PRIVATE_KEY_SUFFIX = ".key";
    private static final String PUBLIC_KEY_SUFFIX = ".pub";

    /**
     * Generate a new CSR
     *
     * @param request CSR request
     * @return CSR response
     * @throws CertificateException if CSR generation fails
     */
    public CsrResponse generateCSR(CsrRequest request) throws CertificateException {
        validateCsrRequest(request);
        
        try {
            String csrId = UuidGenerator.generate();
            int keySize = request.getKeySize() != null ? request.getKeySize() : 2048;
            
            logger.info("Generating CSR with ID: {}", csrId);
            
            KeyPair keyPair = CertificateUtil.generateKeyPair(keySize);
            PKCS10CertificationRequest csr = CertificateUtil.generateCSR(
                keyPair,
                request.getCommonName(),
                request.getOrganization(),
                request.getCountry(),
                request.getState(),
                request.getLocality()
            );
            
            String csrPem = CertificateUtil.csrToPem(csr);
            String publicKeyPem = CertificateUtil.exportPublicKeyToPem(keyPair.getPublic());
            String privateKeyPem = CertificateUtil.exportPrivateKeyToPem(keyPair.getPrivate());
            
            saveCsrMetadata(csrId, request, keySize);
            saveCsrFiles(csrId, csrPem, privateKeyPem, publicKeyPem);
            
            CsrResponse response = new CsrResponse(csrId, csrPem, publicKeyPem,
                request.getCommonName(), keySize);
            
            logger.info("Successfully generated CSR: {}", csrId);
            return response;
        } catch (CertificateException e) {
            logger.error("Failed to generate CSR", e);
            throw e;
        }
    }

    /**
     * Get a CSR by ID
     *
     * @param csrId CSR ID
     * @return CSR response
     * @throws CertificateException if CSR not found
     */
    public CsrResponse getCSR(String csrId) throws CertificateException {
        try {
            Path metadataPath = getCsrMetadataPath(csrId);
            if (!Files.exists(metadataPath)) {
                throw new CertificateException("CSR not found: " + csrId);
            }
            
            Map<String, Object> metadata = FileStorageUtil.readMetadata(metadataPath);
            Path csrPath = getCsrPath(csrId);
            String csrPem = FileStorageUtil.readFile(csrPath);
            Path pubKeyPath = getPublicKeyPath(csrId);
            String publicKeyPem = FileStorageUtil.readFile(pubKeyPath);
            
            CsrResponse response = new CsrResponse(
                csrId,
                csrPem,
                publicKeyPem,
                (String) metadata.get("commonName"),
                ((Number) metadata.get("keySize")).intValue()
            );
            response.setTimestamp(LocalDateTime.parse((String) metadata.get("createdAt")));
            response.setStatus((String) metadata.get("status"));
            
            logger.debug("Retrieved CSR: {}", csrId);
            return response;
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get CSR", e);
            throw new CertificateException("Failed to get CSR", e);
        }
    }

    /**
     * List all CSRs with pagination
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return list of CSR responses
     */
    public List<CsrResponse> listCSRs(int page, int size) {
        try {
            Path csrsDir = getCsrDirectory();
            if (!Files.exists(csrsDir)) {
                return new ArrayList<>();
            }
            
            List<CsrResponse> csrs = Files.list(csrsDir)
                .filter(p -> p.getFileName().toString().endsWith(METADATA_SUFFIX))
                .map(metadataPath -> {
                    try {
                        String csrId = metadataPath.getFileName().toString()
                            .replace(METADATA_SUFFIX, "");
                        return getCSR(csrId);
                    } catch (Exception e) {
                        logger.warn("Failed to load CSR from metadata", e);
                        return null;
                    }
                })
                .filter(csr -> csr != null)
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
            
            logger.debug("Listed {} CSRs (page: {}, size: {})", csrs.size(), page, size);
            return csrs;
        } catch (Exception e) {
            logger.error("Failed to list CSRs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Delete a CSR by ID
     *
     * @param csrId CSR ID
     * @throws CertificateException if deletion fails
     */
    public void deleteCSR(String csrId) throws CertificateException {
        try {
            FileStorageUtil.deleteFile(getCsrPath(csrId));
            FileStorageUtil.deleteFile(getPrivateKeyPath(csrId));
            FileStorageUtil.deleteFile(getPublicKeyPath(csrId));
            FileStorageUtil.deleteFile(getCsrMetadataPath(csrId));
            
            logger.info("Deleted CSR: {}", csrId);
        } catch (Exception e) {
            logger.error("Failed to delete CSR", e);
            throw new CertificateException("Failed to delete CSR", e);
        }
    }

    /**
     * Get the path for CSR metadata
     *
     * @param csrId CSR ID
     * @return Path to metadata file
     */
    public Path getCsrMetadataPath(String csrId) {
        return Paths.get(getCsrDirectory().toString(), csrId + METADATA_SUFFIX);
    }

    /**
     * Get the path for CSR PEM file
     *
     * @param csrId CSR ID
     * @return Path to CSR file
     */
    private Path getCsrPath(String csrId) {
        return Paths.get(getCsrDirectory().toString(), csrId + CSR_SUFFIX);
    }

    /**
     * Get the path for private key file
     *
     * @param csrId CSR ID
     * @return Path to private key file
     */
    private Path getPrivateKeyPath(String csrId) {
        return Paths.get(getCsrDirectory().toString(), csrId + PRIVATE_KEY_SUFFIX);
    }

    /**
     * Get the path for public key file
     *
     * @param csrId CSR ID
     * @return Path to public key file
     */
    private Path getPublicKeyPath(String csrId) {
        return Paths.get(getCsrDirectory().toString(), csrId + PUBLIC_KEY_SUFFIX);
    }

    /**
     * Get the CSR directory
     *
     * @return Path to CSR directory
     */
    private Path getCsrDirectory() {
        return FileStorageUtil.getSubdirectoryPath(CSRS_DIRECTORY);
    }

    /**
     * Save CSR metadata
     *
     * @param csrId CSR ID
     * @param request CSR request
     * @param keySize key size
     */
    private void saveCsrMetadata(String csrId, CsrRequest request, int keySize) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", csrId);
        metadata.put("type", "CSR");
        metadata.put("commonName", request.getCommonName());
        metadata.put("organization", request.getOrganization());
        metadata.put("country", request.getCountry());
        metadata.put("state", request.getState());
        metadata.put("locality", request.getLocality());
        metadata.put("keySize", keySize);
        metadata.put("createdAt", LocalDateTime.now().toString());
        metadata.put("status", "pending");
        
        FileStorageUtil.writeMetadata(getCsrMetadataPath(csrId), metadata);
        logger.debug("Saved CSR metadata: {}", csrId);
    }

    /**
     * Save CSR files
     *
     * @param csrId CSR ID
     * @param csrPem CSR in PEM format
     * @param privateKeyPem private key in PEM format
     * @param publicKeyPem public key in PEM format
     */
    private void saveCsrFiles(String csrId, String csrPem, String privateKeyPem, String publicKeyPem) {
        FileStorageUtil.ensureDirectoryExists(getCsrDirectory());
        FileStorageUtil.writeFile(getCsrPath(csrId), csrPem);
        FileStorageUtil.writeFile(getPrivateKeyPath(csrId), privateKeyPem);
        FileStorageUtil.writeFile(getPublicKeyPath(csrId), publicKeyPem);
        logger.debug("Saved CSR files: {}", csrId);
    }

    /**
     * Validate CSR request
     *
     * @param request CSR request
     */
    private void validateCsrRequest(CsrRequest request) {
        if (request.getCommonName() == null || request.getCommonName().isEmpty()) {
            throw new ValidationException("Common name is required");
        }
        if (request.getCountry() == null || request.getCountry().isEmpty()) {
            throw new ValidationException("Country is required");
        }
        if (request.getKeySize() != null && 
            (request.getKeySize() != 2048 && request.getKeySize() != 4096)) {
            throw new ValidationException("Key size must be 2048 or 4096");
        }
    }
}
