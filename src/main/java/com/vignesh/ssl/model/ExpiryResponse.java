package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response model for certificate expiry information
 */
public class ExpiryResponse {
    
    @JsonProperty("certificateId")
    private String certificateId;
    
    @JsonProperty("commonName")
    private String commonName;
    
    @JsonProperty("expiryDate")
    private LocalDateTime expiryDate;
    
    @JsonProperty("daysRemaining")
    private Long daysRemaining;
    
    @JsonProperty("status")
    private String status;

    public ExpiryResponse() {}

    public ExpiryResponse(String certificateId, String commonName, LocalDateTime expiryDate,
                         Long daysRemaining, String status) {
        this.certificateId = certificateId;
        this.commonName = commonName;
        this.expiryDate = expiryDate;
        this.daysRemaining = daysRemaining;
        this.status = status;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
