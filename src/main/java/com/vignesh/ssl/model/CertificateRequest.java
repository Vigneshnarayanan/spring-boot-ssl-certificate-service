package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for signing a CSR into a certificate
 */
public class CertificateRequest {
    
    @JsonProperty("csrId")
    private String csrId;
    
    @JsonProperty("validityDays")
    private Integer validityDays;
    
    @JsonProperty("issuerName")
    private String issuerName;

    public CertificateRequest() {}

    public CertificateRequest(String csrId, Integer validityDays, String issuerName) {
        this.csrId = csrId;
        this.validityDays = validityDays;
        this.issuerName = issuerName;
    }

    public String getCsrId() {
        return csrId;
    }

    public void setCsrId(String csrId) {
        this.csrId = csrId;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }
}
