package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.util.CertificateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CertificateValidationServiceTest {
    
    private CertificateValidationService validationService;
    private X509Certificate testCertificate;

    @BeforeEach
    void setUp() throws CertificateException {
        validationService = new CertificateValidationService();
        
        KeyPair keyPair = CertificateUtil.generateKeyPair(2048);
        org.bouncycastle.asn1.x500.X500Name subject = 
            new org.bouncycastle.asn1.x500.X500NameBuilder(org.bouncycastle.asn1.x500.style.BCStyle.INSTANCE)
                .addRDN(org.bouncycastle.asn1.x500.style.BCStyle.CN, "test.com")
                .build();
        testCertificate = CertificateUtil.generateSelfSignedCertificate(keyPair, subject, 365);
    }

    @Test
    void testValidateCertificate() {
        boolean isValid = validationService.validateCertificate(testCertificate);
        assertTrue(isValid);
    }

    @Test
    void testCheckExpiry() {
        CertificateValidationService.ExpiryStatus status = validationService.checkExpiry(testCertificate);
        
        assertNotNull(status);
        assertNotNull(status.getExpiryDate());
        assertTrue(status.getDaysRemaining() > 0);
    }

    @Test
    void testVerifySelfSigned() {
        boolean isSelfSigned = validationService.verifySelfSigned(testCertificate);
        assertTrue(isSelfSigned);
    }
}
