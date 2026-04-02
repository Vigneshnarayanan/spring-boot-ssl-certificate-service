package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response model for statistics endpoint
 */
public class StatsResponse {
    
    @JsonProperty("totalCsrs")
    private Long totalCsrs;
    
    @JsonProperty("totalCertificates")
    private Long totalCertificates;
    
    @JsonProperty("totalKeystores")
    private Long totalKeystores;
    
    @JsonProperty("expiringCertificates")
    private Long expiringCertificates;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public StatsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public StatsResponse(Long totalCsrs, Long totalCertificates, Long totalKeystores,
                        Long expiringCertificates) {
        this.totalCsrs = totalCsrs;
        this.totalCertificates = totalCertificates;
        this.totalKeystores = totalKeystores;
        this.expiringCertificates = expiringCertificates;
        this.timestamp = LocalDateTime.now();
    }

    public Long getTotalCsrs() {
        return totalCsrs;
    }

    public void setTotalCsrs(Long totalCsrs) {
        this.totalCsrs = totalCsrs;
    }

    public Long getTotalCertificates() {
        return totalCertificates;
    }

    public void setTotalCertificates(Long totalCertificates) {
        this.totalCertificates = totalCertificates;
    }

    public Long getTotalKeystores() {
        return totalKeystores;
    }

    public void setTotalKeystores(Long totalKeystores) {
        this.totalKeystores = totalKeystores;
    }

    public Long getExpiringCertificates() {
        return expiringCertificates;
    }

    public void setExpiringCertificates(Long expiringCertificates) {
        this.expiringCertificates = expiringCertificates;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
