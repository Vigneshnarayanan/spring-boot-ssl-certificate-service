package com.vignesh.ssl.controller;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.ApiResponse;
import com.vignesh.ssl.model.KeystoreRequest;
import com.vignesh.ssl.model.KeystoreResponse;
import com.vignesh.ssl.service.KeystoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for keystore operations
 */
@RestController
@RequestMapping("/api/v1/keystores")
public class KeystoreController {
    
    private static final Logger logger = LoggerFactory.getLogger(KeystoreController.class);
    private final KeystoreService keystoreService;

    public KeystoreController(KeystoreService keystoreService) {
        this.keystoreService = keystoreService;
    }

    /**
     * Generate a new keystore
     *
     * @param request keystore request
     * @return keystore response
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<KeystoreResponse>> generateKeystore(@RequestBody KeystoreRequest request) {
        try {
            logger.info("Received request to generate keystore for certificate: {}", request.getCertificateId());
            validateKeystoreRequest(request);
            
            KeystoreResponse response = keystoreService.generateKeystore(
                request.getCertificateId(),
                request.getKeystoreType(),
                request.getPassword(),
                request.getAlias()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Keystore generated successfully", response));
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (CertificateException e) {
            logger.error("Certificate error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating keystore", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to generate keystore"));
        }
    }

    /**
     * Get a keystore by ID
     *
     * @param keystoreId keystore ID
     * @return keystore response
     */
    @GetMapping("/{keystoreId}")
    public ResponseEntity<ApiResponse<KeystoreResponse>> getKeystore(@PathVariable String keystoreId) {
        try {
            logger.debug("Fetching keystore: {}", keystoreId);
            KeystoreResponse response = keystoreService.getKeystore(keystoreId);
            return ResponseEntity.ok(ApiResponse.success("Keystore retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Error fetching keystore: {}", keystoreId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Keystore not found"));
        }
    }

    /**
     * List all keystores
     *
     * @return list of keystore responses
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<KeystoreResponse>>> listKeystores() {
        try {
            logger.debug("Listing keystores");
            List<KeystoreResponse> responses = keystoreService.listKeystores();
            return ResponseEntity.ok(ApiResponse.success("Keystores retrieved successfully", responses));
        } catch (Exception e) {
            logger.error("Error listing keystores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to list keystores"));
        }
    }

    /**
     * Download a keystore file
     *
     * @param keystoreId keystore ID
     * @return binary keystore file
     */
    @GetMapping("/{keystoreId}/download")
    public ResponseEntity<?> downloadKeystore(@PathVariable String keystoreId) {
        try {
            logger.debug("Downloading keystore: {}", keystoreId);
            byte[] keystoreBytes = keystoreService.exportKeystore(keystoreId);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(keystoreId + ".p12").build().toString())
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(keystoreBytes);
        } catch (CertificateException e) {
            logger.error("Certificate error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Keystore not found"));
        } catch (Exception e) {
            logger.error("Error downloading keystore", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to download keystore"));
        }
    }

    /**
     * Validate keystore request
     *
     * @param request keystore request
     */
    private void validateKeystoreRequest(KeystoreRequest request) {
        if (request.getCertificateId() == null || request.getCertificateId().isEmpty()) {
            throw new ValidationException("Certificate ID is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Keystore password is required");
        }
        if (request.getAlias() == null || request.getAlias().isEmpty()) {
            throw new ValidationException("Certificate alias is required");
        }
    }
}
