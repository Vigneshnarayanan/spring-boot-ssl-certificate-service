package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.model.CsrRequest;
import com.vignesh.ssl.model.CertificateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CertificateServiceTest {
    
    @Autowired
    private CertificateService certificateService;
    
    @Autowired
    private CsrService csrService;

    private String csrId;

    @BeforeEach
    void setUp() throws CertificateException {
        CsrRequest csrRequest = new CsrRequest("test.example.com", "Test", "US", "CA", "SF", 2048);
        csrId = csrService.generateCSR(csrRequest).getCsrId();
    }

    @Test
    void testSignCSR() throws CertificateException {
        CertificateResponse response = certificateService.signCSR(csrId, 365, "Test CA");
        
        assertNotNull(response);
        assertNotNull(response.getCertificateId());
        assertNotNull(response.getCertificatePem());
        assertEquals("active", response.getStatus());
    }

    @Test
    void testGetCertificate() throws CertificateException {
        CertificateResponse signed = certificateService.signCSR(csrId, 365, "Test CA");
        CertificateResponse retrieved = certificateService.getCertificate(signed.getCertificateId());
        
        assertEquals(signed.getCertificateId(), retrieved.getCertificateId());
    }

    @Test
    void testListCertificates() throws CertificateException {
        certificateService.signCSR(csrId, 365, "Test CA");
        
        List<CertificateResponse> certs = certificateService.listCertificates(0, 20);
        
        assertNotNull(certs);
        assertTrue(certs.size() >= 1);
    }

    @Test
    void testExportCertificatePEM() throws CertificateException {
        CertificateResponse signed = certificateService.signCSR(csrId, 365, "Test CA");
        byte[] pemBytes = certificateService.exportCertificate(signed.getCertificateId(), "PEM");
        
        assertNotNull(pemBytes);
        assertTrue(pemBytes.length > 0);
        assertTrue(new String(pemBytes).contains("BEGIN CERTIFICATE"));
    }
}
