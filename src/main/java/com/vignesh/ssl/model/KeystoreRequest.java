package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for keystore generation
 */
public class KeystoreRequest {
    
    @JsonProperty("certificateId")
    private String certificateId;
    
    @JsonProperty("keystoreType")
    private String keystoreType = "PKCS12";
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("alias")
    private String alias;

    public KeystoreRequest() {}

    public KeystoreRequest(String certificateId, String keystoreType, String password, String alias) {
        this.certificateId = certificateId;
        this.keystoreType = keystoreType != null ? keystoreType : "PKCS12";
        this.password = password;
        this.alias = alias;
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
        this.keystoreType = keystoreType != null ? keystoreType : "PKCS12";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
