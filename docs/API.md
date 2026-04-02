# API Documentation

## Overview

This document provides comprehensive reference for all REST API endpoints in the SSL Certificate Service.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, no authentication is required. In production, implement API key or OAuth2 authentication.

---

## Endpoints

### CSR Endpoints

#### Generate CSR

**Endpoint:** `POST /csr/generate`

**Description:** Generate a new Certificate Signing Request (CSR) with a keypair.

**Request Body:**
```json
{
  "commonName": "api.example.com",
  "organization": "MyOrganization",
  "country": "US",
  "state": "California",
  "locality": "San Francisco",
  "keySize": 2048
}
```

**Response:**
```json
{
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "csrPem": "-----BEGIN CERTIFICATE REQUEST-----\n...\n-----END CERTIFICATE REQUEST-----",
  "publicKeyPem": "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----",
  "timestamp": "2026-04-02T10:30:00Z",
  "status": "pending"
}
```

**HTTP Status Codes:**
- `201 Created`: CSR generated successfully
- `400 Bad Request`: Invalid input parameters
- `500 Internal Server Error`: Server error

---

#### Get CSR

**Endpoint:** `GET /csr/{csrId}`

**Description:** Retrieve details of a previously generated CSR.

**Response:**
```json
{
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "commonName": "api.example.com",
  "organization": "MyOrganization",
  "csrPem": "-----BEGIN CERTIFICATE REQUEST-----\n...\n-----END CERTIFICATE REQUEST-----",
  "publicKeyPem": "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----",
  "createdAt": "2026-04-02T10:30:00Z",
  "status": "pending"
}
```

**HTTP Status Codes:**
- `200 OK`: CSR found
- `404 Not Found`: CSR does not exist
- `500 Internal Server Error`: Server error

---

#### List CSRs

**Endpoint:** `GET /csr/list?page=0&size=10`

**Description:** List all CSRs with pagination support.

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Number of items per page (default: 10)

**Response:**
```json
{
  "content": [
    {
      "csrId": "550e8400-e29b-41d4-a716-446655440000",
      "commonName": "api.example.com",
      "organization": "MyOrganization",
      "createdAt": "2026-04-02T10:30:00Z",
      "status": "pending"
    }
  ],
  "totalElements": 42,
  "totalPages": 5,
  "currentPage": 0,
  "pageSize": 10
}
```

---

#### Delete CSR

**Endpoint:** `DELETE /csr/{csrId}`

**Description:** Delete a CSR and its associated files.

**Response:**
```json
{
  "success": true,
  "message": "CSR deleted successfully",
  "csrId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**HTTP Status Codes:**
- `200 OK`: CSR deleted
- `404 Not Found`: CSR does not exist
- `500 Internal Server Error`: Server error

---

### Certificate Endpoints

#### Sign Certificate

**Endpoint:** `POST /certificates/sign`

**Description:** Sign a CSR and generate a self-signed certificate.

**Request Body:**
```json
{
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "validityDays": 365,
  "issuerName": "MyOrganization CA"
}
```

**Response:**
```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "certificatePem": "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----",
  "issuer": "MyOrganization CA",
  "commonName": "api.example.com",
  "validFrom": "2026-04-02T10:30:00Z",
  "validUntil": "2027-04-02T10:30:00Z",
  "fingerprint": "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90",
  "serialNumber": "12345678901234567890",
  "status": "signed"
}
```

**HTTP Status Codes:**
- `201 Created`: Certificate signed successfully
- `400 Bad Request`: Invalid CSR or parameters
- `404 Not Found`: CSR not found
- `500 Internal Server Error`: Server error

---

#### Get Certificate

**Endpoint:** `GET /certificates/{certificateId}`

**Description:** Retrieve a previously signed certificate.

**Response:**
```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "certificatePem": "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----",
  "issuer": "MyOrganization CA",
  "commonName": "api.example.com",
  "validFrom": "2026-04-02T10:30:00Z",
  "validUntil": "2027-04-02T10:30:00Z",
  "fingerprint": "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90",
  "status": "signed"
}
```

---

#### List Certificates

**Endpoint:** `GET /certificates/list?page=0&size=10`

**Description:** List all certificates with pagination.

**Response:** List of certificate objects (see Get Certificate response)

---

#### Renew Certificate

**Endpoint:** `POST /certificates/{certificateId}/renew`

**Description:** Generate a new certificate from the same CSR with extended validity.

**Request Body:**
```json
{
  "validityDays": 730
}
```

**Response:** New certificate object with new certificateId

---

#### Check Certificate Expiry

**Endpoint:** `GET /certificates/{certificateId}/expiry`

**Description:** Check certificate expiration status.

**Response:**
```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "expiryDate": "2027-04-02T10:30:00Z",
  "daysRemaining": 362,
  "status": "valid",
  "warningThreshold": 30
}
```

---

#### Download Certificate

**Endpoint:** `GET /certificates/{certificateId}/download?format=PEM`

**Description:** Download certificate in different formats.

**Query Parameters:**
- `format` (optional): `PEM`, `DER`, or `CRT` (default: `PEM`)

**Response:** Binary file download

**HTTP Status Codes:**
- `200 OK`: Certificate file
- `404 Not Found`: Certificate not found
- `400 Bad Request`: Invalid format

---

### Keystore Endpoints

#### Generate Keystore

**Endpoint:** `POST /keystores/generate`

**Description:** Create a PKCS12 keystore containing certificate and private key.

**Request Body:**
```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "keystoreType": "PKCS12",
  "password": "secure-password-here",
  "alias": "myalias"
}
```

**Response:**
```json
{
  "keystoreId": "770e8400-e29b-41d4-a716-446655440002",
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "type": "PKCS12",
  "size": 4567,
  "alias": "myalias",
  "downloadUrl": "/api/v1/keystores/770e8400-e29b-41d4-a716-446655440002/download",
  "createdAt": "2026-04-02T10:30:00Z"
}
```

---

#### Get Keystore

**Endpoint:** `GET /keystores/{keystoreId}`

**Description:** Get keystore metadata.

**Response:** Keystore object (see Generate Keystore response)

---

#### Download Keystore

**Endpoint:** `GET /keystores/{keystoreId}/download`

**Description:** Download the keystore file.

**Response:** Binary PKCS12 file (.p12)

---

### Health & Status Endpoints

#### Health Check

**Endpoint:** `GET /health`

**Description:** Check service health status.

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2026-04-02T10:30:00Z",
  "services": {
    "cryptography": "UP",
    "fileStorage": "UP",
    "diskSpace": "UP"
  }
}
```

---

#### Service Statistics

**Endpoint:** `GET /stats`

**Description:** Get service usage statistics.

**Response:**
```json
{
  "totalCsrs": 42,
  "totalCertificates": 38,
  "totalKeystores": 25,
  "expiringCertificates": 3,
  "storageUsedMB": 256.5,
  "uptime": "2 days 5 hours",
  "timestamp": "2026-04-02T10:30:00Z"
}
```

---

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2026-04-02T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input: commonName is required",
  "path": "/api/v1/csr/generate"
}
```

### Common Error Codes

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Bad Request | Invalid input parameters |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 500 | Internal Server Error | Server processing error |
| 503 | Service Unavailable | Service temporarily down |

---

## Rate Limiting

Currently not implemented. Will be added in future versions.

---

## Pagination

List endpoints support pagination with default values:
- Default page size: 10
- Maximum page size: 100
- Pages are 0-indexed

Example: `/api/v1/csr/list?page=0&size=50`

---

## Data Formats

### PEM Format
```
-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJAKC1/...
...
-----END CERTIFICATE-----
```

### DER Format
Binary format (base64 or raw bytes)

### CRT Format
Alias for PEM format used in some systems

---

## Timestamps

All timestamps are in ISO 8601 format:
- Format: `YYYY-MM-DDTHH:mm:ssZ`
- Timezone: UTC (Z suffix)
- Example: `2026-04-02T10:30:00Z`

---

## Examples

See [EXAMPLES.md](EXAMPLES.md) for complete end-to-end examples with curl and Java code.
