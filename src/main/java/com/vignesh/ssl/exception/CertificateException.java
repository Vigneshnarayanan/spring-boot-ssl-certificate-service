package com.vignesh.ssl.exception;

/**
 * Custom checked exception for certificate-related errors
 */
public class CertificateException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public CertificateException(String message) {
        super(message);
    }

    public CertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateException(Throwable cause) {
        super(cause);
    }
}
