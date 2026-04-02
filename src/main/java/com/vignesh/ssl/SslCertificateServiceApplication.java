package com.vignesh.ssl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot SSL Certificate Service Application
 * 
 * REST API service for SSL certificate lifecycle management:
 * - CSR (Certificate Signing Request) generation
 * - Certificate signing and creation
 * - Certificate renewal workflows
 * - Keystore generation and management
 * 
 * @author Vignesh Narayanan
 * @version 0.1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.vignesh.ssl")
public class SslCertificateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SslCertificateServiceApplication.class, args);
    }

}
