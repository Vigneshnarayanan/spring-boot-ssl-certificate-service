package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response model for keystore information
 */
public class KeystoreResponse {
    
    @JsonProperty("keystoreId")
    private String keystoreId;
    
    @JsonProperty("certificateId")
    private String certificateId;
    
    @JsonProperty("keystoreType")
    private String keystoreType;
    
    @JsonProperty("alias")
    private String alias;
    
    @JsonProperty("commonName")
    private String commonName;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    
    @JsonProperty("status")
    private String status;

    public KeystoreResponse() {}

    public KeystoreResponse(String keystoreId, String certificateId, String keystoreType,
                           String alias, String commonName, LocalDateTime createdAt,
                           LocalDateTime expiresAt) {
        this.keystoreId = keystoreId;
        this.certificateId = certificateId;
        this.keystoreType = keystoreType;
        this.alias = alias;
        this.commonName = commonName;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = "active";
    }

    public String getKeystoreId() {
        return keystoreId;
    }

    public void setKeystoreId(String keystoreId) {
        this.keystoreId = keystoreId;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
