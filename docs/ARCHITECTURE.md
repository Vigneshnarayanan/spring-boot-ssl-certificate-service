# Architecture

## System Design

The SSL Certificate Service follows a layered architecture pattern with clear separation of concerns.

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                           │
│  ┌──────────────┬─────────────┬─────────────┬────────────┐  │
│  │ CsrController│CertController│KeystoreCtrl│HealthCtrl │  │
│  └──────────────┴─────────────┴─────────────┴────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────────┐
│              Service/Business Logic Layer                   │
│  ┌──────────────┬──────────────┬──────────────┬─────────┐  │
│  │  CsrService  │CertService   │KeystoreService│Validation│  │
│  └──────────────┴──────────────┴──────────────┴─────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────────┐
│              Utility/Helper Layer                           │
│  ┌──────────────┬──────────────┬──────────────┬─────────┐  │
│  │CertificateUtil│KeystoreUtil  │FileStorageUtil│UuidGen  │  │
│  └──────────────┴──────────────┴──────────────┴─────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────────┐
│               Data/Storage Layer                            │
│     ┌─────────────────────────────────────────────────┐    │
│     │  File System (certificates-storage/)            │    │
│     │  ├── csrs/           (CSR files & metadata)     │    │
│     │  ├── certificates/   (Cert files & keys)       │    │
│     │  ├── keystores/      (PKCS12 keystores)        │    │
│     │  └── logs/           (Activity logs)           │    │
│     └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Descriptions

### Controllers (REST API Layer)

**Purpose:** Handle HTTP requests and responses

#### CsrController
- **Path:** `/api/v1/csr`
- **Responsibilities:**
  - Route CSR generation requests
  - Validate input parameters
  - Return CSR responses
  - Handle CSR listing and deletion

#### CertificateController
- **Path:** `/api/v1/certificates`
- **Responsibilities:**
  - Route certificate signing requests
  - Manage certificate operations (get, list, renew, export)
  - Check certificate expiry
  - Handle downloads

#### KeystoreController
- **Path:** `/api/v1/keystores`
- **Responsibilities:**
  - Generate keystores from certificates
  - Manage keystore metadata
  - Serve keystore downloads

#### HealthController
- **Path:** `/api/v1/health`, `/api/v1/stats`
- **Responsibilities:**
  - Report service health status
  - Provide usage statistics
  - Monitor component availability

---

### Services (Business Logic Layer)

**Purpose:** Implement business logic and orchestrate operations

#### CsrService
```
generateCSR(request) → CsrResponse
├─ Generate RSA keypair (2048 or 4096 bit)
├─ Create PKCS#10 CSR
├─ Store files and metadata
└─ Return response with IDs

getCSR(id) → CsrResponse
└─ Retrieve from file storage

listCSRs(page, size) → List<CsrResponse>
└─ Scan storage with pagination

deleteCSR(id) → void
└─ Remove files and metadata
```

#### CertificateService
```
signCSR(csrId, validityDays, issuer) → CertificateResponse
├─ Load CSR from storage
├─ Parse PKCS#10 request
├─ Generate self-signed X.509 certificate
├─ Validate certificate
└─ Store files and metadata

getCertificate(id) → CertificateResponse
└─ Retrieve from storage

listCertificates(page, size) → List<CertificateResponse>
└─ List all certificates

renewCertificate(id, newValidityDays) → CertificateResponse
├─ Load original certificate
├─ Get associated CSR
├─ Sign new certificate with extended validity
└─ Return new certificate

exportCertificate(id, format) → byte[]
├─ Load certificate
├─ Convert format (PEM, DER, CRT)
└─ Return bytes
```

#### CertificateValidationService
```
validateCSR(pem) → boolean
├─ Parse PEM format
├─ Validate PKCS#10 structure
└─ Return validity

validateCertificate(cert) → boolean
├─ Check X.509 structure
├─ Verify digital signature
└─ Return validity

checkExpiry(cert) → ExpiryStatus
├─ Calculate days remaining
├─ Determine status (valid, expiring_soon, expired)
└─ Return status

verifySelfSigned(cert) → boolean
├─ Check if issuer == subject
└─ Verify self-signature
```

#### KeystoreService
```
generateKeystore(certId, type, password, alias) → KeystoreResponse
├─ Load certificate and private key
├─ Create KeyStore object
├─ Add certificate entry
├─ Set password
└─ Store file and metadata

getKeystore(id) → KeystoreResponse
└─ Retrieve metadata

exportKeystore(id) → byte[]
└─ Load and return keystore bytes
```

---

### Utilities (Utility Layer)

**Purpose:** Provide reusable cryptographic and file operations

#### CertificateUtil
- **Bouncy Castle wrapper** for X.509 certificate operations
- **Methods:**
  - `generateKeyPair(size)` - Create RSA keypairs
  - `generateCSR()` - Create PKCS#10 requests
  - `generateSelfSignedCertificate()` - Create X.509 certificates
  - `certificateToPem()` - Convert to PEM format
  - `pemToCertificate()` - Parse PEM format
  - `getSubjectName()`, `getIssuerName()` - Extract DN information
  - `calculateFingerprint()` - Generate certificate fingerprints

#### KeystoreUtil
- **KeyStore operations**
- **Methods:**
  - `createKeystore()` - Create PKCS12 keystores
  - `exportKeystore()` - Export to bytes
  - `getKeystorePath()` - Resolve storage path

#### FileStorageUtil
- **File I/O and metadata management**
- **Methods:**
  - `writeFile()` - Write PEM/DER files
  - `readFile()` - Read certificate files
  - `writeMetadata()` - Write JSON metadata
  - `readMetadata()` - Read JSON metadata
  - `ensureDirectoryExists()` - Create storage paths

#### UuidGenerator
- **Unique ID generation**
- **Methods:**
  - `generateId()` - Create UUID-based IDs

---

### Models (Data Models)

#### Request Models
- `CsrRequest` - CSR generation parameters
- `CertificateRequest` - Certificate signing parameters
- `KeystoreRequest` - Keystore generation parameters

#### Response Models
- `CsrResponse` - CSR generation response
- `CertificateResponse` - Certificate operation response
- `KeystoreResponse` - Keystore response
- `ExpiryResponse` - Certificate expiry information
- `HealthResponse` - Service health status
- `StatsResponse` - Service statistics
- `ApiResponse<T>` - Generic response wrapper
- `ErrorResponse` - Error details

---

### Exception Handling

#### Custom Exceptions
- `CertificateException` - Checked exception for certificate operations
- `ValidationException` - Input validation failures

#### GlobalExceptionHandler
- **@ControllerAdvice** for centralized error handling
- **Converts exceptions to error responses**
- **Implements HTTP status codes:**
  - 400 Bad Request (validation errors)
  - 404 Not Found (resource not found)
  - 409 Conflict (duplicate resources)
  - 500 Internal Server Error (unexpected errors)

---

## Data Flow

### CSR Generation Flow

```
HTTP Request (POST /api/v1/csr/generate)
            │
            ▼
CsrController.generateCSR()
            │
            ▼
Input Validation
            │
            ▼
CsrService.generateCSR()
            │
            ├─▶ CertificateUtil.generateKeyPair()
            │           │
            │           └─▶ Return KeyPair
            │
            ├─▶ CertificateUtil.generateCSR()
            │           │
            │           └─▶ Return PKCS10CertificationRequest
            │
            ├─▶ Convert to PEM format
            │
            ├─▶ FileStorageUtil.writeFile()
            │           │
            │           └─▶ Store in csrs/{id}/ directory
            │
            └─▶ FileStorageUtil.writeMetadata()
                        │
                        └─▶ Store JSON metadata
                        
            ▼
Return CsrResponse
            │
            ▼
HTTP 201 Created Response
```

---

### Certificate Signing Flow

```
HTTP Request (POST /api/v1/certificates/sign)
            │
            ▼
CertificateController.signCertificate()
            │
            ▼
Input Validation
            │
            ▼
CertificateService.signCSR()
            │
            ├─▶ CsrService.getCSR(csrId)
            │           │
            │           └─▶ Load from storage
            │
            ├─▶ CertificateUtil.pemToCertificate()
            │           │
            │           └─▶ Parse PKCS#10 CSR
            │
            ├─▶ CertificateUtil.generateSelfSignedCertificate()
            │           │
            │           └─▶ Create X.509 certificate
            │
            ├─▶ CertificateValidationService.validateCertificate()
            │           │
            │           └─▶ Verify structure and signature
            │
            ├─▶ Convert to PEM format
            │
            ├─▶ FileStorageUtil.writeFile() (certificate + key)
            │           │
            │           └─▶ Store in certificates/{id}/ directory
            │
            └─▶ FileStorageUtil.writeMetadata()
                        │
                        └─▶ Store JSON metadata
                        
            ▼
Return CertificateResponse
            │
            ▼
HTTP 201 Created Response
```

---

### Keystore Generation Flow

```
HTTP Request (POST /api/v1/keystores/generate)
            │
            ▼
KeystoreController.generateKeystore()
            │
            ▼
Input Validation
            │
            ▼
KeystoreService.generateKeystore()
            │
            ├─▶ CertificateService.getCertificate(certId)
            │           │
            │           └─▶ Load from storage
            │
            ├─▶ Extract private key from certificate storage
            │
            ├─▶ KeystoreUtil.createKeystore()
            │       │
            │       ├─▶ Create KeyStore object (PKCS12)
            │       │
            │       ├─▶ Add certificate entry
            │       │
            │       └─▶ Set password protection
            │
            ├─▶ FileStorageUtil.writeFile(keystore.p12)
            │           │
            │           └─▶ Store in keystores/{id}/ directory
            │
            └─▶ FileStorageUtil.writeMetadata()
                        │
                        └─▶ Store JSON metadata
                        
            ▼
Return KeystoreResponse with download URL
            │
            ▼
HTTP 201 Created Response
```

---

## Storage Structure

### Directory Layout

```
certificates-storage/
│
├── csrs/                          # Certificate Signing Requests
│   └── {csrId}/
│       ├── request.pem           # PKCS#10 CSR in PEM format
│       ├── public-key.pem        # Extracted public key
│       └── metadata.json         # Metadata: CN, org, dates, status
│
├── certificates/                  # Signed Certificates
│   └── {certificateId}/
│       ├── certificate.pem       # X.509 certificate in PEM
│       ├── certificate.der       # X.509 certificate in DER
│       ├── private-key.pem       # Private key (PEM encrypted)
│       └── metadata.json         # Metadata: issuer, dates, fingerprint
│
├── keystores/                     # PKCS12 Keystores
│   └── {keystoreId}/
│       ├── keystore.p12          # PKCS12 keystore file
│       └── metadata.json         # Metadata: alias, type, size
│
└── logs/                          # Activity Logs
    ├── activity.log              # All operations logged
    └── errors.log                # Error details
```

### Metadata File Format

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "CSR or CERTIFICATE",
  "commonName": "example.com",
  "organization": "Organization Name",
  "country": "US",
  "state": "California",
  "locality": "San Francisco",
  "createdAt": "2026-04-02T10:30:00Z",
  "expiresAt": "2027-04-02T10:30:00Z",
  "status": "pending or signed or expired",
  "relationships": {
    "csrId": "550e8400-e29b-41d4-a716-446655440000",
    "renewedFrom": "original-certificate-id"
  },
  "fingerprint": "AB:CD:EF:12:34:56:78:90:...",
  "validityDays": 365,
  "issuer": "Certificate Issuer Name"
}
```

---

## Security Considerations

### 1. Private Key Protection
- Private keys stored with file permissions 0600 (owner read/write only)
- Consider encrypting at-rest in future versions
- Never transmitted in cleartext

### 2. Certificate Validation
- All certificates validated before storage
- Self-signature verification enforced
- Expiry checking on retrieval

### 3. File System Security
- Storage directory restricted to application user
- Metadata files protect sensitive information
- Activity logs for auditing

### 4. Input Validation
- All request parameters validated
- Common Name must be FQDN format
- Key sizes limited to 2048 or 4096 bits

### 5. Future Improvements
- Implement API authentication (API keys or OAuth2)
- Add HTTPS/TLS support
- Implement request rate limiting
- Add audit logging for all operations
- Support database backend instead of files
- Integrate with secure key vaults (HashiCorp Vault, AWS KMS)

---

## Performance Characteristics

| Operation | Time | Notes |
|-----------|------|-------|
| Generate 2048-bit CSR | ~100ms | CPU-bound |
| Generate 4096-bit CSR | ~500ms | CPU-bound |
| Sign Certificate | ~200ms | Bouncy Castle signature |
| Create Keystore | ~50ms | File I/O |
| List CSRs (100 items) | ~50ms | File system scan |
| Get Health Status | ~10ms | In-memory check |

---

## Scalability

### Current Limitations (Single Instance)
- File-based storage
- No database backend
- No clustering support
- Limited to filesystem capacity

### Future Improvements
- Database backend (PostgreSQL)
- Distributed cache (Redis)
- Load balancing support
- Horizontal scaling capability
- S3/Cloud storage backend

---

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 4.0.3 |
| Cryptography | Bouncy Castle | 1.70 |
| API Docs | Springdoc OpenAPI | 2.x |
| Testing | JUnit 5 | 5.9.x |
| Build | Maven | 3.9.10+ |
| Logging | SLF4J + Logback | Latest |

---

## Design Patterns Used

1. **Layered Architecture** - Clear separation of concerns
2. **Service Layer Pattern** - Business logic isolation
3. **Utility Classes** - Reusable cryptographic operations
4. **DTO Pattern** - Request/Response objects
5. **Exception Translation** - Domain exceptions to HTTP responses
6. **Repository Pattern** - File storage abstraction
7. **Singleton Pattern** - Utility classes and services

---

## Future Enhancements

1. **Database Persistence**
   - PostgreSQL for metadata
   - Hybrid storage model

2. **Advanced Cryptography**
   - ECC support
   - Hardware security module (HSM) integration
   - FIPS 140-2 compliance

3. **CA Integration**
   - Let's Encrypt ACME support
   - DigiCert API integration
   - Multi-CA support

4. **Authentication & Authorization**
   - API key authentication
   - OAuth2/OIDC support
   - Role-based access control (RBAC)

5. **Monitoring & Alerting**
   - Prometheus metrics
   - Certificate expiry alerts
   - Webhook notifications
   - Alert integration (PagerDuty, Slack)

6. **Compliance**
   - Certificate revocation list (CRL) support
   - OCSP responder
   - Audit logging and retention
   - Compliance reporting

---

For detailed examples, see [EXAMPLES.md](EXAMPLES.md)
For API reference, see [API.md](API.md)
