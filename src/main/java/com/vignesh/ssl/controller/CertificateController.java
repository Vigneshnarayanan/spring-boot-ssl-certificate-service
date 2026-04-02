package com.vignesh.ssl.controller;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.ApiResponse;
import com.vignesh.ssl.model.CertificateRequest;
import com.vignesh.ssl.model.CertificateResponse;
import com.vignesh.ssl.model.ExpiryResponse;
import com.vignesh.ssl.service.CertificateService;
import com.vignesh.ssl.service.CertificateValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for certificate operations
 */
@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateController {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);
    private final CertificateService certificateService;
    private final CertificateValidationService validationService;

    public CertificateController(CertificateService certificateService,
                               CertificateValidationService validationService) {
        this.certificateService = certificateService;
        this.validationService = validationService;
    }

    /**
     * Sign a CSR into a certificate
     *
     * @param request certificate request
     * @return certificate response
     */
    @PostMapping("/sign")
    public ResponseEntity<ApiResponse<CertificateResponse>> signCSR(@RequestBody CertificateRequest request) {
        try {
            logger.info("Received request to sign CSR: {}", request.getCsrId());
            validateCertificateRequest(request);
            
            CertificateResponse response = certificateService.signCSR(
                request.getCsrId(),
                request.getValidityDays(),
                request.getIssuerName()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certificate signed successfully", response));
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (CertificateException e) {
            logger.error("Certificate error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error signing CSR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to sign CSR"));
        }
    }

    /**
     * Get a certificate by ID
     *
     * @param certificateId certificate ID
     * @return certificate response
     */
    @GetMapping("/{certificateId}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificate(@PathVariable String certificateId) {
        try {
            logger.debug("Fetching certificate: {}", certificateId);
            CertificateResponse response = certificateService.getCertificate(certificateId);
            return ResponseEntity.ok(ApiResponse.success("Certificate retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Error fetching certificate: {}", certificateId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Certificate not found"));
        }
    }

    /**
     * List all certificates with pagination
     *
     * @param page page number (default: 0)
     * @param size page size (default: 20)
     * @return list of certificate responses
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> listCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.debug("Listing certificates: page={}, size={}", page, size);
            List<CertificateResponse> responses = certificateService.listCertificates(page, size);
            return ResponseEntity.ok(ApiResponse.success("Certificates retrieved successfully", responses));
        } catch (Exception e) {
            logger.error("Error listing certificates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to list certificates"));
        }
    }

    /**
     * Renew a certificate
     *
     * @param certificateId certificate ID
     * @param validityDays new validity period
     * @return new certificate response
     */
    @PostMapping("/{certificateId}/renew")
    public ResponseEntity<ApiResponse<CertificateResponse>> renewCertificate(
            @PathVariable String certificateId,
            @RequestParam(defaultValue = "365") int validityDays) {
        try {
            logger.info("Received request to renew certificate: {}", certificateId);
            validateValidityDays(validityDays);
            
            CertificateResponse response = certificateService.renewCertificate(certificateId, validityDays);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certificate renewed successfully", response));
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (CertificateException e) {
            logger.error("Certificate error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error renewing certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to renew certificate"));
        }
    }

    /**
     * Get certificate expiry information
     *
     * @param certificateId certificate ID
     * @return expiry response
     */
    @GetMapping("/{certificateId}/expiry")
    public ResponseEntity<ApiResponse<ExpiryResponse>> getCertificateExpiry(@PathVariable String certificateId) {
        try {
            logger.debug("Fetching certificate expiry: {}", certificateId);
            var certificate = certificateService.getCertificateAsX509(certificateId);
            var expiryStatus = validationService.checkExpiry(certificate);
            var certResponse = certificateService.getCertificate(certificateId);
            
            ExpiryResponse response = new ExpiryResponse(
                certificateId,
                certResponse.getCommonName(),
                expiryStatus.getExpiryDate(),
                expiryStatus.getDaysRemaining(),
                expiryStatus.getStatus()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Expiry information retrieved", response));
        } catch (Exception e) {
            logger.error("Error fetching certificate expiry: {}", certificateId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Certificate not found"));
        }
    }

    /**
     * Download a certificate in specified format
     *
     * @param certificateId certificate ID
     * @param format PEM, DER, or CRT (default: PEM)
     * @return binary certificate file
     */
    @GetMapping("/{certificateId}/download")
    public ResponseEntity<?> downloadCertificate(
            @PathVariable String certificateId,
            @RequestParam(defaultValue = "PEM") String format) {
        try {
            logger.debug("Downloading certificate: {}, format: {}", certificateId, format);
            byte[] certificateBytes = certificateService.exportCertificate(certificateId, format);
            
            String fileExtension = format.equalsIgnoreCase("PEM") ? "pem" : "crt";
            String filename = certificateId + "." + fileExtension;
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(filename).build().toString())
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(certificateBytes);
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (CertificateException e) {
            logger.error("Certificate error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Certificate not found"));
        } catch (Exception e) {
            logger.error("Error downloading certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to download certificate"));
        }
    }

    /**
     * Validate certificate request
     *
     * @param request certificate request
     */
    private void validateCertificateRequest(CertificateRequest request) {
        if (request.getCsrId() == null || request.getCsrId().isEmpty()) {
            throw new ValidationException("CSR ID is required");
        }
        if (request.getValidityDays() == null) {
            throw new ValidationException("Validity days is required");
        }
        validateValidityDays(request.getValidityDays());
        if (request.getIssuerName() == null || request.getIssuerName().isEmpty()) {
            throw new ValidationException("Issuer name is required");
        }
    }

    /**
     * Validate validity days
     *
     * @param validityDays validity days to validate
     */
    private void validateValidityDays(int validityDays) {
        if (validityDays < 1 || validityDays > 3650) {
            throw new ValidationException("Validity days must be between 1 and 3650");
        }
    }
}
