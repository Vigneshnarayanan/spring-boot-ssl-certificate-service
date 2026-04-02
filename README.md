# 🔐 Spring Boot SSL Certificate Service

[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.10-C71A36.svg)](https://maven.apache.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-blue.svg)](https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service/actions)

A professional Spring Boot REST API service for complete SSL certificate lifecycle management. Generate certificate signing requests (CSR), create self-signed certificates, manage renewals, and generate keystores for seamless Spring Boot integration.

**Perfect for:** Learning, testing, development environments, and demonstrations of certificate management patterns in Java/Spring Boot.

---

## ✨ Features

### 🔑 Certificate Signing Request (CSR) Generation
- Generate CSR with RSA keypairs (2048/4096 bit)
- Store CSRs with metadata and tracking
- Retrieve CSR in PEM format
- List all generated CSRs with pagination

### 📜 Certificate Signing & Management
- Create self-signed X.509 certificates
- Sign CSRs with custom validity periods
- Export certificates in multiple formats (PEM, DER, CRT)
- Track certificate lifecycle and metadata
- Support for certificate chains

### 🔄 Certificate Renewal
- Generate new certificates from existing CSRs
- Track renewal history
- Compare old vs new certificates
- Automated expiry notifications

### 🔐 Keystore Management
- Generate PKCS12 keystores from certificates
- Import certificates with private keys
- Export keystores with password protection
- Direct integration with Spring Boot SSL configuration

### 📊 Monitoring & Health
- Certificate expiry tracking and alerts
- Service health checks
- Performance metrics via Actuator
- Detailed activity logging

### 📖 API Documentation
- Swagger/OpenAPI documentation
- Interactive API explorer
- Complete endpoint reference
- Usage examples included

---

## 🚀 Quick Start

### Prerequisites
- **Java 17** or higher
- **Maven 3.9.10** or higher
- **Git**

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service.git
cd spring-boot-ssl-certificate-service
```

2. **Build the project:**
```bash
mvn clean package
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Swagger/OpenAPI UI
```
http://localhost:8080/swagger-ui.html
```

### 🔑 CSR (Certificate Signing Request) Endpoints

#### Generate New CSR
```http
POST /api/v1/csr/generate
Content-Type: application/json

{
  "commonName": "example.com",
  "organization": "My Organization",
  "country": "AU",
  "state": "New South Wales",
  "locality": "Sydney",
  "keySize": 2048
}
```

**Response (201 Created):**
```json
{
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "csrPem": "-----BEGIN CERTIFICATE REQUEST-----\n...",
  "publicKeyPem": "-----BEGIN PUBLIC KEY-----\n...",
  "commonName": "example.com",
  "keySize": 2048,
  "timestamp": "2026-04-02T08:00:00",
  "status": "pending"
}
```

#### Get CSR Details
```http
GET /api/v1/csr/{csrId}
```

#### List All CSRs
```http
GET /api/v1/csr/list?page=0&size=10
```

---

### 📜 Certificate Management Endpoints

#### Sign a Certificate
```http
POST /api/v1/certificates/sign
Content-Type: application/json

{
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "validityDays": 365,
  "issuerName": "Local CA"
}
```

**Response (201 Created):**
```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "certificatePem": "-----BEGIN CERTIFICATE-----\n...",
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "commonName": "example.com",
  "validFrom": "2026-04-02T08:00:00",
  "validUntil": "2027-04-02T08:00:00",
  "issuer": "Local CA",
  "status": "signed"
}
```

#### Get Certificate Details
```http
GET /api/v1/certificates/{certificateId}
```

#### List All Certificates
```http
GET /api/v1/certificates/list?page=0&size=10
```

#### Check Certificate Expiry
```http
GET /api/v1/certificates/{certificateId}/expiry
```

**Response:**
```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "expiryDate": "2027-04-02T08:00:00",
  "expiresIn": "365 days",
  "isExpiring": false,
  "daysUntilExpiry": 365
}
```

---

### 🔐 Keystore Management Endpoints

#### Generate Keystore
```http
POST /api/v1/keystores/generate
Content-Type: application/json

{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "keystoreType": "PKCS12",
  "password": "secure-password-123"
}
```

**Response (201 Created):**
```json
{
  "keystoreId": "770e8400-e29b-41d4-a716-446655440002",
  "type": "PKCS12",
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "size": "4567 bytes",
  "createdAt": "2026-04-02T08:00:00",
  "downloadUrl": "/api/v1/keystores/770e8400-e29b-41d4-a716-446655440002/download"
}
```

#### Download Keystore
```http
GET /api/v1/keystores/{keystoreId}/download
```

---

### 📊 Health & Status Endpoints

#### Service Health
```http
GET /api/v1/health
```

**Response:**
```json
{
  "status": "UP",
  "services": {
    "cryptography": "UP",
    "storage": "UP",
    "api": "UP"
  }
}
```

#### Service Statistics
```http
GET /api/v1/stats
```

**Response:**
```json
{
  "totalCsrs": 42,
  "totalCertificates": 38,
  "totalKeyStores": 25,
  "expiringCertificates": 3,
  "uptime": "2 days 5 hours"
}
```

---

## 💻 Usage Examples

### Complete Workflow: CSR → Sign → Keystore

#### Step 1: Generate a CSR
```bash
curl -X POST http://localhost:8080/api/v1/csr/generate \
  -H "Content-Type: application/json" \
  -d '{
    "commonName": "myapp.local",
    "organization": "My Company",
    "country": "AU",
    "state": "NSW",
    "locality": "Sydney",
    "keySize": 2048
  }'
```

**Save the `csrId` from the response**

#### Step 2: Sign the Certificate
```bash
curl -X POST http://localhost:8080/api/v1/certificates/sign \
  -H "Content-Type: application/json" \
  -d '{
    "csrId": "550e8400-e29b-41d4-a716-446655440000",
    "validityDays": 365,
    "issuerName": "My Local CA"
  }'
```

**Save the `certificateId` from the response**

#### Step 3: Generate Keystore
```bash
curl -X POST http://localhost:8080/api/v1/keystores/generate \
  -H "Content-Type: application/json" \
  -d '{
    "certificateId": "660e8400-e29b-41d4-a716-446655440001",
    "keystoreType": "PKCS12",
    "password": "my-keystore-password"
  }'
```

#### Step 4: Download Keystore
```bash
curl -X GET http://localhost:8080/api/v1/keystores/770e8400-e29b-41d4-a716-446655440002/download \
  -o myapp-keystore.p12
```

---

## 🏗️ Project Structure

```
spring-boot-ssl-certificate-service/
├── src/
│   ├── main/
│   │   ├── java/com/vignesh/ssl/
│   │   │   ├── SslCertificateServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── CsrController.java
│   │   │   │   ├── CertificateController.java
│   │   │   │   ├── KeystoreController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── service/
│   │   │   │   ├── CsrService.java
│   │   │   │   ├── CertificateService.java
│   │   │   │   ├── CertificateValidationService.java
│   │   │   │   └── KeystoreService.java
│   │   │   ├── model/
│   │   │   │   ├── CsrRequest.java
│   │   │   │   ├── CsrResponse.java
│   │   │   │   ├── CertificateRequest.java
│   │   │   │   └── CertificateResponse.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── util/
│   │   │       ├── CertificateUtil.java
│   │   │       └── FileStorageUtil.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/vignesh/ssl/
│           ├── CsrServiceTest.java
│           ├── CertificateServiceTest.java
│           └── IntegrationTest.java
├── .github/
│   └── workflows/
│       ├── build-test.yaml
│       ├── security-scan.yaml
│       └── demo-workflow.yaml
├── docs/
│   ├── SETUP.md
│   ├── API.md
│   ├── EXAMPLES.md
│   └── ARCHITECTURE.md
├── certificates-storage/
│   ├── csrs/
│   ├── certificates/
│   ├── keystores/
│   └── logs/
├── pom.xml
├── README.md
├── LICENSE
└── .gitignore
```

---

## 🛠️ Development

### Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Run with Debug
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

### Generate Coverage Report
```bash
mvn clean test jacoco:report
# Open: target/site/jacoco/index.html
```

---

## 🔒 Security Considerations

- **Private Keys**: Never leave the application; protected with restricted file permissions
- **Passwords**: Use environment variables or secure vaults (not hardcoded)
- **File Storage**: Use local file system for development; upgrade to encrypted database for production
- **API Authentication**: Currently open for learning; add OAuth2/JWT for production
- **HTTPS**: Enable SSL/TLS for the service itself in production

---

## 📊 Technology Stack

| Component | Version |
|-----------|---------|
| Java | 17 |
| Spring Boot | 4.0.3 |
| Maven | 3.9.10+ |
| Bouncy Castle | 1.77+ |
| JUnit 5 | 5.x |
| Mockito | 5.x |

---

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Docker (Coming Soon)
```bash
docker build -t ssl-certificate-service:0.1.0 .
docker run -p 8080:8080 ssl-certificate-service:0.1.0
```

### Kubernetes (Coming Soon)
```bash
kubectl apply -f k8s/deployment.yaml
```

---

## 📝 License

MIT License - see [LICENSE](LICENSE) file for details

---

## 👤 Author

**Vignesh Narayanan**
- GitHub: [@Vigneshnarayanan](https://github.com/Vigneshnarayanan)
- LinkedIn: [linkedin.com/in/vigneshnarayanann](https://linkedin.com/in/vigneshnarayanann)

---

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

---

<div align="center">

**[⬆ Back to Top](#-spring-boot-ssl-certificate-service)**

Made with ❤️ for learning SSL/TLS certificate management in Java

[![GitHub](https://img.shields.io/badge/github-follow-lightgrey.svg?style=flat-square)](https://github.com/Vigneshnarayanan)
[![LinkedIn](https://img.shields.io/badge/linkedin-connect-blue.svg?style=flat-square)](https://linkedin.com/in/vigneshnarayanann)

</div>
