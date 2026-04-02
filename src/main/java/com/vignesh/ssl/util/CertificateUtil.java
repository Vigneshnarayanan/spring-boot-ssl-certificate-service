package com.vignesh.ssl.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility class for Bouncy Castle certificate operations
 */
public class CertificateUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateUtil.class);
    private static final String RSA_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    static {
        if (org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME == null) {
            org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME.hashCode();
        }
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    private CertificateUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Generate a new RSA key pair
     *
     * @param keySize key size (2048 or 4096)
     * @return KeyPair
     * @throws CertificateException if key generation fails
     */
    public static KeyPair generateKeyPair(int keySize) throws com.vignesh.ssl.exception.CertificateException {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM, "BC");
            keyGen.initialize(keySize, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            logger.debug("Generated {} bit RSA key pair", keySize);
            return keyPair;
        } catch (NoSuchAlgorithmException | java.security.NoSuchProviderException e) {
            logger.error("Failed to generate key pair", e);
            throw new com.vignesh.ssl.exception.CertificateException("Failed to generate key pair", e);
        }
    }

    /**
     * Generate a Certificate Signing Request (CSR)
     *
     * @param keyPair the key pair
     * @param commonName common name
     * @param org organization
     * @param country country code
     * @param state state/province
     * @param locality locality
     * @return PKCS10CertificationRequest
     * @throws CertificateException if CSR generation fails
     */
    public static PKCS10CertificationRequest generateCSR(KeyPair keyPair, String commonName,
            String org, String country, String state, String locality) throws com.vignesh.ssl.exception.CertificateException {
        try {
            X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
            if (country != null && !country.isEmpty()) builder.addRDN(BCStyle.C, country);
            if (state != null && !state.isEmpty()) builder.addRDN(BCStyle.ST, state);
            if (locality != null && !locality.isEmpty()) builder.addRDN(BCStyle.L, locality);
            if (org != null && !org.isEmpty()) builder.addRDN(BCStyle.O, org);
            builder.addRDN(BCStyle.CN, commonName);
            
            X500Name subject = builder.build();
            PKCS10CertificationRequestBuilder csrBuilder = 
                new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
            
            PKCS10CertificationRequest csr = csrBuilder.build(
                new org.bouncycastle.operator.jcajce.JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                    .setProvider("BC").build(keyPair.getPrivate())
            );
            
            logger.debug("Generated CSR for {}", commonName);
            return csr;
        } catch (Exception e) {
            logger.error("Failed to generate CSR", e);
            throw new com.vignesh.ssl.exception.CertificateException("Failed to generate CSR", e);
        }
    }

    /**
     * Generate a self-signed certificate
     *
     * @param keyPair the key pair
     * @param subject subject name
     * @param validityDays validity period in days
     * @return X509Certificate
     * @throws CertificateException if certificate generation fails
     */
    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair,
            X500Name subject, long validityDays) throws com.vignesh.ssl.exception.CertificateException {
        try {
            Instant now = Instant.now();
            Date notBefore = Date.from(now);
            Date notAfter = Date.from(now.plus(validityDays, ChronoUnit.DAYS));
            
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(
                keyPair.getPublic().getEncoded()
            );
            
            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                subject,
                new BigInteger(64, new SecureRandom()),
                notBefore,
                notAfter,
                subject,
                publicKeyInfo
            );
            
            X509CertificateHolder certHolder = certBuilder.build(
                new org.bouncycastle.operator.jcajce.JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                    .setProvider("BC").build(keyPair.getPrivate())
            );
            
            X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC").getCertificate(certHolder);
            
            logger.debug("Generated self-signed certificate for {}", subject);
            return cert;
        } catch (Exception e) {
            logger.error("Failed to generate self-signed certificate", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to generate self-signed certificate", e);
        }
    }

    /**
     * Convert certificate to PEM format
     *
     * @param certificate X509Certificate
     * @return PEM string
     * @throws CertificateException if conversion fails
     */
    public static String certificateToPem(X509Certificate certificate) throws com.vignesh.ssl.exception.CertificateException {
        try {
            StringWriter sw = new StringWriter();
            JcaPEMWriter writer = new JcaPEMWriter(sw);
            writer.writeObject(certificate);
            writer.close();
            return sw.toString();
        } catch (IOException e) {
            logger.error("Failed to convert certificate to PEM", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to convert certificate to PEM", e);
        }
    }

    /**
     * Convert PEM string to X509Certificate
     *
     * @param pemString PEM formatted certificate
     * @return X509Certificate
     * @throws CertificateException if conversion fails
     */
    public static X509Certificate pemToCertificate(String pemString) throws com.vignesh.ssl.exception.CertificateException {
        try {
            PemReader reader = new PemReader(new StringReader(pemString));
            PemObject pemObject = reader.readPemObject();
            reader.close();
            
            if (pemObject == null) {
                throw new com.vignesh.ssl.exception.CertificateException("Invalid PEM format");
            }
            
            X509CertificateHolder holder = new X509CertificateHolder(pemObject.getContent());
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        } catch (com.vignesh.ssl.exception.CertificateException e) {
            throw e;
        } catch (IOException | java.security.cert.CertificateException e) {
            logger.error("Failed to parse certificate from PEM", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to parse certificate from PEM", e);
        }
    }

    /**
     * Export private key to PEM format
     *
     * @param privateKey private key
     * @return PEM string
     * @throws CertificateException if export fails
     */
    public static String exportPrivateKeyToPem(PrivateKey privateKey) throws com.vignesh.ssl.exception.CertificateException {
        try {
            StringWriter sw = new StringWriter();
            JcaPEMWriter writer = new JcaPEMWriter(sw);
            writer.writeObject(privateKey);
            writer.close();
            return sw.toString();
        } catch (IOException e) {
            logger.error("Failed to export private key to PEM", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to export private key to PEM", e);
        }
    }

    /**
     * Convert CSR to PEM format
     *
     * @param csr PKCS10CertificationRequest
     * @return PEM string
     * @throws CertificateException if conversion fails
     */
    public static String csrToPem(PKCS10CertificationRequest csr) throws com.vignesh.ssl.exception.CertificateException {
        try {
            StringWriter sw = new StringWriter();
            JcaPEMWriter writer = new JcaPEMWriter(sw);
            writer.writeObject(csr);
            writer.close();
            return sw.toString();
        } catch (IOException e) {
            logger.error("Failed to convert CSR to PEM", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to convert CSR to PEM", e);
        }
    }

    /**
     * Export public key to PEM format
     *
     * @param publicKey public key
     * @return PEM string
     * @throws CertificateException if export fails
     */
    public static String exportPublicKeyToPem(PublicKey publicKey) throws com.vignesh.ssl.exception.CertificateException {
        try {
            StringWriter sw = new StringWriter();
            JcaPEMWriter writer = new JcaPEMWriter(sw);
            writer.writeObject(publicKey);
            writer.close();
            return sw.toString();
        } catch (IOException e) {
            logger.error("Failed to export public key to PEM", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to export public key to PEM", e);
        }
    }

    /**
     * Get X500Name from certificate subject
     *
     * @param certificate X509Certificate
     * @return X500Name
     */
    public static X500Name getSubjectName(X509Certificate certificate) {
        try {
            X509CertificateHolder holder = new X509CertificateHolder(certificate.getEncoded());
            return holder.getSubject();
        } catch (Exception e) {
            logger.error("Failed to extract subject name", e);
            return null;
        }
    }

    /**
     * Get X500Name from certificate issuer
     *
     * @param certificate X509Certificate
     * @return X500Name
     */
    public static X500Name getIssuerName(X509Certificate certificate) {
        try {
            X509CertificateHolder holder = new X509CertificateHolder(certificate.getEncoded());
            return holder.getIssuer();
        } catch (Exception e) {
            logger.error("Failed to extract issuer name", e);
            return null;
        }
    }

    /**
     * Calculate fingerprint (SHA-256) of a certificate
     *
     * @param certificate X509Certificate
     * @return fingerprint as hex string
     * @throws CertificateException if fingerprint calculation fails
     */
    public static String calculateFingerprint(X509Certificate certificate) throws com.vignesh.ssl.exception.CertificateException {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(certificate.getEncoded());
            return bytesToHex(digest);
        } catch (Exception e) {
            logger.error("Failed to calculate fingerprint", e);
            throw new com.vignesh.ssl.exception.CertificateException(
                "Failed to calculate fingerprint", e);
        }
    }

    /**
     * Extract Common Name (CN) from X500Name
     *
     * @param name X500Name
     * @return Common Name or "Unknown" if not found
     */
    public static String getCommonName(X500Name name) {
        if (name == null) return "Unknown";
        org.bouncycastle.asn1.x500.RDN[] rdns = name.getRDNs(BCStyle.CN);
        if (rdns.length > 0) {
            return rdns[0].getFirst().getValue().toString();
        }
        return "Unknown";
    }

    /**
     * Convert bytes to hex string
     *
     * @param bytes byte array
     * @return hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
            if (i < bytes.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
}
