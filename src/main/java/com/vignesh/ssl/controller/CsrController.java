package com.vignesh.ssl.controller;

import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.ApiResponse;
import com.vignesh.ssl.model.CsrRequest;
import com.vignesh.ssl.model.CsrResponse;
import com.vignesh.ssl.service.CsrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for CSR operations
 */
@RestController
@RequestMapping("/api/v1/csr")
public class CsrController {
    
    private static final Logger logger = LoggerFactory.getLogger(CsrController.class);
    private final CsrService csrService;

    public CsrController(CsrService csrService) {
        this.csrService = csrService;
    }

    /**
     * Generate a new CSR
     *
     * @param request CSR request
     * @return CSR response
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<CsrResponse>> generateCSR(@RequestBody CsrRequest request) {
        try {
            logger.info("Received request to generate CSR for CN: {}", request.getCommonName());
            CsrResponse response = csrService.generateCSR(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CSR generated successfully", response));
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating CSR", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to generate CSR"));
        }
    }

    /**
     * Get a CSR by ID
     *
     * @param csrId CSR ID
     * @return CSR response
     */
    @GetMapping("/{csrId}")
    public ResponseEntity<ApiResponse<CsrResponse>> getCSR(@PathVariable String csrId) {
        try {
            logger.debug("Fetching CSR: {}", csrId);
            CsrResponse response = csrService.getCSR(csrId);
            return ResponseEntity.ok(ApiResponse.success("CSR retrieved successfully", response));
        } catch (Exception e) {
            logger.error("Error fetching CSR: {}", csrId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("CSR not found"));
        }
    }

    /**
     * List all CSRs with pagination
     *
     * @param page page number (default: 0)
     * @param size page size (default: 20)
     * @return list of CSR responses
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CsrResponse>>> listCSRs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.debug("Listing CSRs: page={}, size={}", page, size);
            List<CsrResponse> responses = csrService.listCSRs(page, size);
            return ResponseEntity.ok(ApiResponse.success("CSRs retrieved successfully", responses));
        } catch (Exception e) {
            logger.error("Error listing CSRs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to list CSRs"));
        }
    }

    /**
     * Delete a CSR by ID
     *
     * @param csrId CSR ID
     * @return success response
     */
    @DeleteMapping("/{csrId}")
    public ResponseEntity<ApiResponse<Void>> deleteCSR(@PathVariable String csrId) {
        try {
            logger.info("Deleting CSR: {}", csrId);
            csrService.deleteCSR(csrId);
            return ResponseEntity.ok(ApiResponse.success("CSR deleted successfully", null));
        } catch (Exception e) {
            logger.error("Error deleting CSR: {}", csrId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete CSR"));
        }
    }
}
