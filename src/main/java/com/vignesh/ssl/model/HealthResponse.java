package com.vignesh.ssl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response model for health check endpoint
 */
public class HealthResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("services")
    private Map<String, String> services;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public HealthResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public HealthResponse(String status, Map<String, String> services) {
        this.status = status;
        this.services = services;
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
