package com.vignesh.ssl.controller;

import com.vignesh.ssl.model.ApiResponse;
import com.vignesh.ssl.model.HealthResponse;
import com.vignesh.ssl.model.StatsResponse;
import com.vignesh.ssl.service.CsrService;
import com.vignesh.ssl.service.CertificateService;
import com.vignesh.ssl.service.KeystoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for health and statistics endpoints
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private final CsrService csrService;
    private final CertificateService certificateService;
    private final KeystoreService keystoreService;

    public HealthController(CsrService csrService, CertificateService certificateService,
                          KeystoreService keystoreService) {
        this.csrService = csrService;
        this.certificateService = certificateService;
        this.keystoreService = keystoreService;
    }

    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        try {
            logger.debug("Health check requested");
            Map<String, String> services = new HashMap<>();
            services.put("csr_service", "UP");
            services.put("certificate_service", "UP");
            services.put("keystore_service", "UP");
            services.put("storage", checkStorageHealth() ? "UP" : "DOWN");
            
            HealthResponse response = new HealthResponse("UP", services);
            return ResponseEntity.ok(ApiResponse.success("Health check passed", response));
        } catch (Exception e) {
            logger.error("Health check failed", e);
            Map<String, String> services = new HashMap<>();
            services.put("status", "DOWN");
            
            HealthResponse response = new HealthResponse("DOWN", services);
            return ResponseEntity.status(500).body(ApiResponse.error("Health check failed"));
        }
    }

    /**
     * Statistics endpoint
     *
     * @return statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> stats() {
        try {
            logger.debug("Statistics requested");
            
            long totalCsrs = countFiles("certificates-storage/csrs", ".json");
            long totalCerts = countFiles("certificates-storage/certificates", ".json");
            long totalKeystores = countFiles("certificates-storage/keystores", ".json");
            long expiringCerts = countExpiringCertificates();
            
            StatsResponse response = new StatsResponse(totalCsrs, totalCerts, totalKeystores, expiringCerts);
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Statistics retrieval failed", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to retrieve statistics"));
        }
    }

    /**
     * Check storage health
     *
     * @return true if storage is accessible
     */
    private boolean checkStorageHealth() {
        try {
            Files.createDirectories(Paths.get("certificates-storage"));
            return true;
        } catch (Exception e) {
            logger.error("Storage health check failed", e);
            return false;
        }
    }

    /**
     * Count files with specific extension in directory
     *
     * @param directory directory path
     * @param extension file extension
     * @return file count
     */
    private long countFiles(String directory, String extension) {
        try {
            return Files.list(Paths.get(directory))
                .filter(p -> p.getFileName().toString().endsWith(extension))
                .count();
        } catch (Exception e) {
            logger.debug("Directory not found or error reading: {}", directory);
            return 0;
        }
    }

    /**
     * Count expiring certificates (within 30 days)
     *
     * @return count of expiring certificates
     */
    private long countExpiringCertificates() {
        try {
            return certificateService.listCertificates(0, Integer.MAX_VALUE).stream()
                .filter(cert -> {
                    long daysToExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDateTime.now(), cert.getExpiresAt());
                    return daysToExpiry <= 30 && daysToExpiry > 0;
                })
                .count();
        } catch (Exception e) {
            logger.debug("Error counting expiring certificates", e);
            return 0;
        }
    }
}
