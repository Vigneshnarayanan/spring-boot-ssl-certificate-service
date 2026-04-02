package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response model for certificate information
 */
public class CertificateResponse {
    
    @JsonProperty("certificateId")
    private String certificateId;
    
    @JsonProperty("certificatePem")
    private String certificatePem;
    
    @JsonProperty("commonName")
    private String commonName;
    
    @JsonProperty("issuer")
    private String issuer;
    
    @JsonProperty("serialNumber")
    private String serialNumber;
    
    @JsonProperty("issuedAt")
    private LocalDateTime issuedAt;
    
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    
    @JsonProperty("validityDays")
    private Integer validityDays;
    
    @JsonProperty("fingerprint")
    private String fingerprint;
    
    @JsonProperty("status")
    private String status;

    public CertificateResponse() {}

    public CertificateResponse(String certificateId, String certificatePem, String commonName,
                             String issuer, String serialNumber, LocalDateTime issuedAt,
                             LocalDateTime expiresAt, Integer validityDays, String fingerprint) {
        this.certificateId = certificateId;
        this.certificatePem = certificatePem;
        this.commonName = commonName;
        this.issuer = issuer;
        this.serialNumber = serialNumber;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.validityDays = validityDays;
        this.fingerprint = fingerprint;
        this.status = "active";
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
