package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Request model for CSR (Certificate Signing Request) generation
 */
public class CsrRequest {
    
    @JsonProperty("commonName")
    private String commonName;
    
    @JsonProperty("organization")
    private String organization;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("locality")
    private String locality;
    
    @JsonProperty("keySize")
    private Integer keySize = 2048;

    public CsrRequest() {}

    public CsrRequest(String commonName, String organization, String country, 
                     String state, String locality, Integer keySize) {
        this.commonName = commonName;
        this.organization = organization;
        this.country = country;
        this.state = state;
        this.locality = locality;
        this.keySize = keySize != null ? keySize : 2048;
    }

    // Getters and Setters
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize != null ? keySize : 2048;
    }
}
