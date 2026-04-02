package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Response model for CSR (Certificate Signing Request)
 */
public class CsrResponse {
    
    @JsonProperty("csrId")
    private String csrId;
    
    @JsonProperty("csrPem")
    private String csrPem;
    
    @JsonProperty("publicKeyPem")
    private String publicKeyPem;
    
    @JsonProperty("commonName")
    private String commonName;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("status")
    private String status = "pending";
    
    @JsonProperty("keySize")
    private Integer keySize;

    public CsrResponse() {}

    public CsrResponse(String csrId, String csrPem, String publicKeyPem, 
                      String commonName, Integer keySize) {
        this.csrId = csrId;
        this.csrPem = csrPem;
        this.publicKeyPem = publicKeyPem;
        this.commonName = commonName;
        this.keySize = keySize;
        this.timestamp = LocalDateTime.now();
        this.status = "pending";
    }

    // Getters and Setters
    public String getCsrId() {
        return csrId;
    }

    public void setCsrId(String csrId) {
        this.csrId = csrId;
    }

    public String getCsrPem() {
        return csrPem;
    }

    public void setCsrPem(String csrPem) {
        this.csrPem = csrPem;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }
}
