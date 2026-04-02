package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.util.CertificateUtil;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Service for validating certificates and CSRs
 */
@Service
public class CertificateValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateValidationService.class);

    /**
     * Validate a CSR in PEM format
     *
     * @param csrPem CSR in PEM format
     * @return true if valid
     */
    public boolean validateCSR(String csrPem) {
        try {
            PEMParser parser = new PEMParser(new StringReader(csrPem));
            Object obj = parser.readObject();
            parser.close();
            
            if (obj instanceof PKCS10CertificationRequest) {
                logger.debug("CSR validation successful");
                return true;
            }
            logger.warn("Invalid CSR format");
            return false;
        } catch (Exception e) {
            logger.warn("CSR validation failed", e);
            return false;
        }
    }

    /**
     * Validate a certificate
     *
     * @param certificate X509Certificate
     * @return true if valid
     */
    public boolean validateCertificate(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            logger.debug("Certificate validation successful");
            return true;
        } catch (Exception e) {
            logger.warn("Certificate validation failed", e);
            return false;
        }
    }

    /**
     * Check certificate expiry status
     *
     * @param certificate X509Certificate
     * @return ExpiryStatus object with days remaining and status
     */
    public ExpiryStatus checkExpiry(X509Certificate certificate) {
        LocalDateTime expiryDate = LocalDateTime.ofInstant(
            certificate.getNotAfter().toInstant(), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        
        long daysRemaining = ChronoUnit.DAYS.between(now, expiryDate);
        String status = determineExpiryStatus(daysRemaining);
        
        logger.debug("Certificate expiry check: {} days remaining, status: {}", daysRemaining, status);
        return new ExpiryStatus(expiryDate, daysRemaining, status);
    }

    /**
     * Verify if certificate is self-signed
     *
     * @param certificate X509Certificate
     * @return true if self-signed
     */
    public boolean verifySelfSigned(X509Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            logger.debug("Certificate is self-signed");
            return true;
        } catch (Exception e) {
            logger.debug("Certificate is not self-signed");
            return false;
        }
    }

    /**
     * Determine expiry status based on days remaining
     *
     * @param daysRemaining days remaining
     * @return status string
     */
    private String determineExpiryStatus(long daysRemaining) {
        if (daysRemaining < 0) {
            return "EXPIRED";
        } else if (daysRemaining <= 7) {
            return "EXPIRING_SOON";
        } else if (daysRemaining <= 30) {
            return "EXPIRING";
        } else {
            return "VALID";
        }
    }

    /**
     * Inner class for expiry status
     */
    public static class ExpiryStatus {
        public LocalDateTime expiryDate;
        public long daysRemaining;
        public String status;

        public ExpiryStatus(LocalDateTime expiryDate, long daysRemaining, String status) {
            this.expiryDate = expiryDate;
            this.daysRemaining = daysRemaining;
            this.status = status;
        }

        public LocalDateTime getExpiryDate() {
            return expiryDate;
        }

        public long getDaysRemaining() {
            return daysRemaining;
        }

        public String getStatus() {
            return status;
        }
    }
}
