package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.CsrRequest;
import com.vignesh.ssl.model.CsrResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CsrServiceTest {
    
    @Autowired
    private CsrService csrService;

    private CsrRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CsrRequest("example.com", "Example", "US", "CA", "SF", 2048);
    }

    @Test
    void testGenerateCSRWithValidRequest() throws CertificateException {
        CsrResponse response = csrService.generateCSR(validRequest);
        
        assertNotNull(response);
        assertNotNull(response.getCsrId());
        assertNotNull(response.getCsrPem());
        assertEquals("example.com", response.getCommonName());
        assertEquals(2048, response.getKeySize());
    }

    @Test
    void testGenerateCSRWithoutCommonName() {
        validRequest.setCommonName(null);
        assertThrows(ValidationException.class, () -> csrService.generateCSR(validRequest));
    }

    @Test
    void testGetCSRByID() throws CertificateException {
        CsrResponse generated = csrService.generateCSR(validRequest);
        CsrResponse retrieved = csrService.getCSR(generated.getCsrId());
        
        assertEquals(generated.getCsrId(), retrieved.getCsrId());
        assertEquals(generated.getCommonName(), retrieved.getCommonName());
    }

    @Test
    void testListCSRs() throws CertificateException {
        csrService.generateCSR(validRequest);
        csrService.generateCSR(validRequest);
        
        List<CsrResponse> csrs = csrService.listCSRs(0, 20);
        
        assertNotNull(csrs);
        assertTrue(csrs.size() >= 1);
    }

    @Test
    void testDeleteCSR() throws CertificateException {
        CsrResponse generated = csrService.generateCSR(validRequest);
        String csrId = generated.getCsrId();
        
        csrService.deleteCSR(csrId);
        
        assertThrows(CertificateException.class, () -> csrService.getCSR(csrId));
    }
}
