package com.vignesh.ssl.service;

import com.vignesh.ssl.exception.CertificateException;
import com.vignesh.ssl.exception.ValidationException;
import com.vignesh.ssl.model.CertificateResponse;
import com.vignesh.ssl.util.CertificateUtil;
import com.vignesh.ssl.util.FileStorageUtil;
import com.vignesh.ssl.util.UuidGenerator;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing certificate operations
 */
@Service
public class CertificateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);
    private static final String CERTIFICATES_DIRECTORY = "certificates";
    private static final String CERT_SUFFIX = ".crt";
    private static final String KEY_SUFFIX = ".key";
    private static final String METADATA_SUFFIX = ".json";
    
    private final CsrService csrService;

    public CertificateService(CsrService csrService) {
        this.csrService = csrService;
    }

    /**
     * Sign a CSR into a certificate
     *
     * @param csrId CSR ID
     * @param validityDays validity period in days
     * @param issuerName issuer name
     * @return certificate response
     * @throws CertificateException if signing fails
     */
    public CertificateResponse signCSR(String csrId, int validityDays, String issuerName) throws CertificateException {
        try {
            if (validityDays < 1 || validityDays > 3650) {
                throw new ValidationException("Validity days must be between 1 and 3650");
            }
            
            String certificateId = UuidGenerator.generate();
            logger.info("Signing CSR {} into certificate {}", csrId, certificateId);
            
            Path csrMetadataPath = csrService.getCsrMetadataPath(csrId);
            if (!Files.exists(csrMetadataPath)) {
                throw new CertificateException("CSR not found: " + csrId);
            }
            
            Map<String, Object> csrMetadata = FileStorageUtil.readMetadata(csrMetadataPath);
            Path csrPath = Paths.get(FileStorageUtil.getSubdirectoryPath("csrs").toString(), csrId + ".csr");
            String csrPem = FileStorageUtil.readFile(csrPath);
            
            PEMParser pemParser = new PEMParser(new StringReader(csrPem));
            PKCS10CertificationRequest csr = (PKCS10CertificationRequest) pemParser.readObject();
            pemParser.close();
            
            X500Name subject = csr.getSubject();
            X500Name issuer = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, issuerName)
                .build();
            
            SubjectPublicKeyInfo publicKeyInfo = csr.getSubjectPublicKeyInfo();
            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer,
                new BigInteger(64, new java.security.SecureRandom()),
                Date.from(Instant.now()),
                Date.from(Instant.now().plus(validityDays, ChronoUnit.DAYS)),
                subject,
                publicKeyInfo
            );
            
            KeyPair tempKeyPair = CertificateUtil.generateKeyPair(2048);
            X509CertificateHolder certHolder = certBuilder.build(
                new org.bouncycastle.operator.jcajce.JcaContentSignerBuilder("SHA256WithRSA")
                    .setProvider("BC").build(tempKeyPair.getPrivate())
            );
            
            X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC").getCertificate(certHolder);
            
            String certificatePem = CertificateUtil.certificateToPem(certificate);
            String privateKeyPem = CertificateUtil.exportPrivateKeyToPem(tempKeyPair.getPrivate());
            
            saveCertificateMetadata(certificateId, certificate, validityDays, issuerName, csrId);
            saveCertificateFiles(certificateId, certificatePem, privateKeyPem);
            
            CertificateResponse response = createCertificateResponse(certificateId, certificate,
                certificatePem, validityDays);
            
            logger.info("Successfully signed CSR into certificate: {}", certificateId);
            return response;
        } catch (CertificateException e) {
            throw e;
        } catch (ValidationException e) {
            throw new CertificateException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to sign CSR", e);
            throw new CertificateException("Failed to sign CSR", e);
        }
    }

    /**
     * Get a certificate by ID
     *
     * @param certificateId certificate ID
     * @return certificate response
     * @throws CertificateException if certificate not found
     */
    public CertificateResponse getCertificate(String certificateId) throws CertificateException {
        try {
            Path metadataPath = getCertificateMetadataPath(certificateId);
            if (!Files.exists(metadataPath)) {
                throw new CertificateException("Certificate not found: " + certificateId);
            }
            
            Map<String, Object> metadata = FileStorageUtil.readMetadata(metadataPath);
            Path certPath = getCertificatePath(certificateId);
            String certificatePem = FileStorageUtil.readFile(certPath);
            
            X509Certificate certificate = CertificateUtil.pemToCertificate(certificatePem);
            CertificateResponse response = createCertificateResponse(certificateId, certificate,
                certificatePem, ((Number) metadata.get("validityDays")).intValue());
            response.setStatus((String) metadata.get("status"));
            
            logger.debug("Retrieved certificate: {}", certificateId);
            return response;
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get certificate", e);
            throw new CertificateException("Failed to get certificate", e);
        }
    }

    /**
     * Get certificate as X509Certificate object
     *
     * @param certificateId certificate ID
     * @return X509Certificate
     * @throws CertificateException if certificate not found
     */
    public X509Certificate getCertificateAsX509(String certificateId) throws CertificateException {
        try {
            Path certPath = getCertificatePath(certificateId);
            if (!Files.exists(certPath)) {
                throw new CertificateException("Certificate not found: " + certificateId);
            }
            
            String certificatePem = FileStorageUtil.readFile(certPath);
            return CertificateUtil.pemToCertificate(certificatePem);
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get certificate as X509", e);
            throw new CertificateException("Failed to get certificate", e);
        }
    }

    /**
     * List all certificates with pagination
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return list of certificate responses
     */
    public List<CertificateResponse> listCertificates(int page, int size) {
        try {
            Path certsDir = getCertificateDirectory();
            if (!Files.exists(certsDir)) {
                return new ArrayList<>();
            }
            
            List<CertificateResponse> certs = Files.list(certsDir)
                .filter(p -> p.getFileName().toString().endsWith(METADATA_SUFFIX))
                .map(metadataPath -> {
                    try {
                        String certId = metadataPath.getFileName().toString()
                            .replace(METADATA_SUFFIX, "");
                        return getCertificate(certId);
                    } catch (Exception e) {
                        logger.warn("Failed to load certificate", e);
                        return null;
                    }
                })
                .filter(cert -> cert != null)
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
            
            logger.debug("Listed {} certificates (page: {}, size: {})", certs.size(), page, size);
            return certs;
        } catch (Exception e) {
            logger.error("Failed to list certificates", e);
            return new ArrayList<>();
        }
    }

    /**
     * Renew a certificate
     *
     * @param certificateId certificate ID
     * @param newValidityDays new validity period in days
     * @return new certificate response
     * @throws CertificateException if renewal fails
     */
    public CertificateResponse renewCertificate(String certificateId, int newValidityDays) throws CertificateException {
        try {
            Path metadataPath = getCertificateMetadataPath(certificateId);
            if (!Files.exists(metadataPath)) {
                throw new CertificateException("Certificate not found: " + certificateId);
            }
            
            Map<String, Object> metadata = FileStorageUtil.readMetadata(metadataPath);
            X509Certificate oldCert = getCertificateAsX509(certificateId);
            
            String newCertId = UuidGenerator.generate();
            logger.info("Renewing certificate {} as {}", certificateId, newCertId);
            
            X500Name subject = CertificateUtil.getSubjectName(oldCert);
            KeyPair keyPair = CertificateUtil.generateKeyPair(2048);
            
            X509Certificate newCert = CertificateUtil.generateSelfSignedCertificate(
                keyPair, subject, newValidityDays
            );
            
            String certificatePem = CertificateUtil.certificateToPem(newCert);
            String privateKeyPem = CertificateUtil.exportPrivateKeyToPem(keyPair.getPrivate());
            
            saveCertificateMetadata(newCertId, newCert, newValidityDays,
                (String) metadata.get("issuer"), (String) metadata.get("originalCsrId"));
            saveCertificateFiles(newCertId, certificatePem, privateKeyPem);
            
            CertificateResponse response = createCertificateResponse(newCertId, newCert,
                certificatePem, newValidityDays);
            
            logger.info("Successfully renewed certificate: {}", newCertId);
            return response;
        } catch (CertificateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to renew certificate", e);
            throw new CertificateException("Failed to renew certificate", e);
        }
    }

    /**
     * Export certificate in different formats
     *
     * @param certificateId certificate ID
     * @param format PEM, DER, or CRT
     * @return certificate bytes
     * @throws CertificateException if export fails
     */
    public byte[] exportCertificate(String certificateId, String format) throws CertificateException {
        try {
            X509Certificate certificate = getCertificateAsX509(certificateId);
            
            return switch (format.toUpperCase()) {
                case "PEM" -> CertificateUtil.certificateToPem(certificate).getBytes();
                case "DER", "CRT" -> certificate.getEncoded();
                default -> throw new ValidationException("Invalid format: " + format);
            };
        } catch (ValidationException e) {
            throw new CertificateException(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to export certificate", e);
            throw new CertificateException("Failed to export certificate", e);
        }
    }

    /**
     * Get the path for certificate metadata
     *
     * @param certificateId certificate ID
     * @return Path to metadata file
     */
    public Path getCertificateMetadataPath(String certificateId) {
        return Paths.get(getCertificateDirectory().toString(), certificateId + METADATA_SUFFIX);
    }

    /**
     * Get the path for certificate PEM file
     *
     * @param certificateId certificate ID
     * @return Path to certificate file
     */
    private Path getCertificatePath(String certificateId) {
        return Paths.get(getCertificateDirectory().toString(), certificateId + CERT_SUFFIX);
    }

    /**
     * Get the path for private key file
     *
     * @param certificateId certificate ID
     * @return Path to private key file
     */
    private Path getPrivateKeyPath(String certificateId) {
        return Paths.get(getCertificateDirectory().toString(), certificateId + KEY_SUFFIX);
    }

    /**
     * Get the certificate directory
     *
     * @return Path to certificate directory
     */
    private Path getCertificateDirectory() {
        return FileStorageUtil.getSubdirectoryPath(CERTIFICATES_DIRECTORY);
    }

    /**
     * Extract common name from X500Name
     *
     * @param x500Name X500Name
     * @return common name string
     */
    private String extractCommonName(org.bouncycastle.asn1.x500.X500Name x500Name) {
        if (x500Name == null) {
            return "Unknown";
        }
        org.bouncycastle.asn1.x500.RDN[] rdns = x500Name.getRDNs();
        if (rdns.length > 0) {
            org.bouncycastle.asn1.x500.RDN rdn = rdns[0];
            if (rdn.getFirst() != null) {
                return rdn.getFirst().getValue().toString();
            }
        }
        return "Unknown";
    }

    /**
     * Save certificate metadata
     *
     * @param certificateId certificate ID
     * @param certificate X509Certificate
     * @param validityDays validity days
     * @param issuer issuer name
     * @param originalCsrId original CSR ID (if any)
     */
    private void saveCertificateMetadata(String certificateId, X509Certificate certificate,
                                        int validityDays, String issuer, String originalCsrId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", certificateId);
        metadata.put("type", "CERTIFICATE");
        metadata.put("commonName", extractCommonName(CertificateUtil.getSubjectName(certificate)));
        metadata.put("issuer", issuer);
        metadata.put("serialNumber", certificate.getSerialNumber().toString());
        metadata.put("issuedAt", LocalDateTime.now().toString());
        metadata.put("expiresAt", LocalDateTime.ofInstant(
            certificate.getNotAfter().toInstant(), ZoneId.systemDefault()).toString());
        metadata.put("validityDays", validityDays);
        metadata.put("status", "active");
        metadata.put("originalCsrId", originalCsrId);
        metadata.put("createdAt", LocalDateTime.now().toString());
        
        FileStorageUtil.writeMetadata(getCertificateMetadataPath(certificateId), metadata);
        logger.debug("Saved certificate metadata: {}", certificateId);
    }

    /**
     * Save certificate files
     *
     * @param certificateId certificate ID
     * @param certificatePem certificate in PEM format
     * @param privateKeyPem private key in PEM format
     */
    private void saveCertificateFiles(String certificateId, String certificatePem, String privateKeyPem) {
        FileStorageUtil.ensureDirectoryExists(getCertificateDirectory());
        FileStorageUtil.writeFile(getCertificatePath(certificateId), certificatePem);
        FileStorageUtil.writeFile(getPrivateKeyPath(certificateId), privateKeyPem);
        logger.debug("Saved certificate files: {}", certificateId);
    }

    /**
     * Create a CertificateResponse from X509Certificate
     *
     * @param certificateId certificate ID
     * @param certificate X509Certificate
     * @param certificatePem certificate PEM
     * @param validityDays validity days
     * @return CertificateResponse
     */
    private CertificateResponse createCertificateResponse(String certificateId, X509Certificate certificate,
                                                         String certificatePem, int validityDays) throws Exception {
        LocalDateTime issuedAt = LocalDateTime.ofInstant(
            certificate.getNotBefore().toInstant(), ZoneId.systemDefault());
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
            certificate.getNotAfter().toInstant(), ZoneId.systemDefault());
        
        String fingerprint = CertificateUtil.calculateFingerprint(certificate);
        String commonName = extractCommonName(CertificateUtil.getSubjectName(certificate));
        String issuer = extractCommonName(CertificateUtil.getIssuerName(certificate));
        
        return new CertificateResponse(
            certificateId,
            certificatePem,
            commonName,
            issuer,
            certificate.getSerialNumber().toString(),
            issuedAt,
            expiresAt,
            validityDays,
            fingerprint
        );
    }
}
