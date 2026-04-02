# Examples

Complete end-to-end examples for using the SSL Certificate Service.

## Example 1: Generate CSR

### Using Curl

```bash
curl -X POST http://localhost:8080/api/v1/csr/generate \
  -H "Content-Type: application/json" \
  -d '{
    "commonName": "api.example.com",
    "organization": "MyOrganization",
    "country": "US",
    "state": "California",
    "locality": "San Francisco",
    "keySize": 2048
  }'
```

### Response

```json
{
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "csrPem": "-----BEGIN CERTIFICATE REQUEST-----\nMIIC...\n-----END CERTIFICATE REQUEST-----",
  "publicKeyPem": "-----BEGIN PUBLIC KEY-----\nMIIB...\n-----END PUBLIC KEY-----",
  "timestamp": "2026-04-02T10:30:00Z",
  "status": "pending"
}
```

---

## Example 2: Sign Certificate from CSR

### Using Curl

```bash
curl -X POST http://localhost:8080/api/v1/certificates/sign \
  -H "Content-Type: application/json" \
  -d '{
    "csrId": "550e8400-e29b-41d4-a716-446655440000",
    "validityDays": 365,
    "issuerName": "MyOrganization CA"
  }'
```

### Response

```json
{
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "csrId": "550e8400-e29b-41d4-a716-446655440000",
  "certificatePem": "-----BEGIN CERTIFICATE-----\nMIID...\n-----END CERTIFICATE-----",
  "issuer": "MyOrganization CA",
  "commonName": "api.example.com",
  "validFrom": "2026-04-02T10:30:00Z",
  "validUntil": "2027-04-02T10:30:00Z",
  "fingerprint": "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90",
  "status": "signed"
}
```

---

## Example 3: Create Keystore from Certificate

### Using Curl

```bash
curl -X POST http://localhost:8080/api/v1/keystores/generate \
  -H "Content-Type: application/json" \
  -d '{
    "certificateId": "660e8400-e29b-41d4-a716-446655440001",
    "keystoreType": "PKCS12",
    "password": "keystore-password",
    "alias": "tomcat"
  }'
```

### Response

```json
{
  "keystoreId": "770e8400-e29b-41d4-a716-446655440002",
  "certificateId": "660e8400-e29b-41d4-a716-446655440001",
  "type": "PKCS12",
  "size": 4567,
  "alias": "tomcat",
  "downloadUrl": "/api/v1/keystores/770e8400-e29b-41d4-a716-446655440002/download",
  "createdAt": "2026-04-02T10:30:00Z"
}
```

### Download Keystore

```bash
curl -X GET http://localhost:8080/api/v1/keystores/770e8400-e29b-41d4-a716-446655440002/download \
  -o myapp.p12
```

---

## Example 4: Full Workflow (Bash Script)

```bash
#!/bin/bash

API="http://localhost:8080/api/v1"

# Step 1: Generate CSR
echo "Generating CSR..."
CSR_RESPONSE=$(curl -s -X POST $API/csr/generate \
  -H "Content-Type: application/json" \
  -d '{
    "commonName": "myapp.example.com",
    "organization": "MyOrganization",
    "country": "US",
    "state": "California",
    "locality": "San Francisco",
    "keySize": 2048
  }')

CSR_ID=$(echo $CSR_RESPONSE | jq -r '.csrId')
echo "CSR ID: $CSR_ID"

# Step 2: Sign Certificate
echo "Signing certificate..."
CERT_RESPONSE=$(curl -s -X POST $API/certificates/sign \
  -H "Content-Type: application/json" \
  -d "{
    \"csrId\": \"$CSR_ID\",
    \"validityDays\": 365,
    \"issuerName\": \"MyOrganization CA\"
  }")

CERT_ID=$(echo $CERT_RESPONSE | jq -r '.certificateId')
echo "Certificate ID: $CERT_ID"

# Step 3: Create Keystore
echo "Creating keystore..."
KEYSTORE_RESPONSE=$(curl -s -X POST $API/keystores/generate \
  -H "Content-Type: application/json" \
  -d "{
    \"certificateId\": \"$CERT_ID\",
    \"keystoreType\": \"PKCS12\",
    \"password\": \"my-keystore-password\",
    \"alias\": \"myapp\"
  }")

KEYSTORE_ID=$(echo $KEYSTORE_RESPONSE | jq -r '.keystoreId')
echo "Keystore ID: $KEYSTORE_ID"

# Step 4: Download Keystore
echo "Downloading keystore..."
curl -s -X GET $API/keystores/$KEYSTORE_ID/download -o myapp-keystore.p12
echo "Keystore saved to myapp-keystore.p12"

# Step 5: Check expiry
echo "Checking certificate expiry..."
curl -s -X GET $API/certificates/$CERT_ID/expiry | jq .
```

### Run Script

```bash
chmod +x create-certificates.sh
./create-certificates.sh
```

---

## Example 5: Java Client Code

### Maven Dependencies

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>
```

### Java Client Example

```java
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class SslCertificateClient {
    
    private static final String API_BASE = "http://localhost:8080/api/v1";
    private static final Gson gson = new Gson();
    private CloseableHttpClient httpClient = HttpClients.createDefault();

    public String generateCSR(String commonName, String organization,
                             String country, String state, String locality,
                             int keySize) throws Exception {
        String requestBody = String.format(
            "{\"commonName\": \"%s\", \"organization\": \"%s\", " +
            "\"country\": \"%s\", \"state\": \"%s\", \"locality\": \"%s\", " +
            "\"keySize\": %d}",
            commonName, organization, country, state, locality, keySize);
        
        HttpPost request = new HttpPost(API_BASE + "/csr/generate");
        request.setEntity(new StringEntity(requestBody, "application/json"));
        
        var response = httpClient.execute(request, httpResponse -> {
            if (httpResponse.getCode() == 201) {
                String body = new String(httpResponse.getEntity().getContent().readAllBytes());
                JsonObject json = gson.fromJson(body, JsonObject.class);
                return json.get("csrId").getAsString();
            }
            return null;
        });
        
        return response;
    }

    public String signCertificate(String csrId, int validityDays,
                                  String issuerName) throws Exception {
        String requestBody = String.format(
            "{\"csrId\": \"%s\", \"validityDays\": %d, \"issuerName\": \"%s\"}",
            csrId, validityDays, issuerName);
        
        HttpPost request = new HttpPost(API_BASE + "/certificates/sign");
        request.setEntity(new StringEntity(requestBody, "application/json"));
        
        var response = httpClient.execute(request, httpResponse -> {
            if (httpResponse.getCode() == 201) {
                String body = new String(httpResponse.getEntity().getContent().readAllBytes());
                JsonObject json = gson.fromJson(body, JsonObject.class);
                return json.get("certificateId").getAsString();
            }
            return null;
        });
        
        return response;
    }

    public String createKeystore(String certificateId, String password,
                                 String alias) throws Exception {
        String requestBody = String.format(
            "{\"certificateId\": \"%s\", \"keystoreType\": \"PKCS12\", " +
            "\"password\": \"%s\", \"alias\": \"%s\"}",
            certificateId, password, alias);
        
        HttpPost request = new HttpPost(API_BASE + "/keystores/generate");
        request.setEntity(new StringEntity(requestBody, "application/json"));
        
        var response = httpClient.execute(request, httpResponse -> {
            if (httpResponse.getCode() == 201) {
                String body = new String(httpResponse.getEntity().getContent().readAllBytes());
                JsonObject json = gson.fromJson(body, JsonObject.class);
                return json.get("keystoreId").getAsString();
            }
            return null;
        });
        
        return response;
    }

    public static void main(String[] args) throws Exception {
        SslCertificateClient client = new SslCertificateClient();

        // Step 1: Generate CSR
        String csrId = client.generateCSR(
            "myapp.example.com",
            "MyOrganization",
            "US",
            "California",
            "San Francisco",
            2048
        );
        System.out.println("CSR ID: " + csrId);

        // Step 2: Sign Certificate
        String certificateId = client.signCertificate(
            csrId,
            365,
            "MyOrganization CA"
        );
        System.out.println("Certificate ID: " + certificateId);

        // Step 3: Create Keystore
        String keystoreId = client.createKeystore(
            certificateId,
            "keystore-password",
            "myapp"
        );
        System.out.println("Keystore ID: " + keystoreId);
    }
}
```

---

## Example 6: Check Certificate Status

### Using Curl

```bash
# Get certificate details
curl -X GET http://localhost:8080/api/v1/certificates/660e8400-e29b-41d4-a716-446655440001 \
  -H "Accept: application/json"

# Check expiry
curl -X GET http://localhost:8080/api/v1/certificates/660e8400-e29b-41d4-a716-446655440001/expiry \
  -H "Accept: application/json"
```

### Response

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

## Example 7: Service Health & Statistics

### Using Curl

```bash
# Check health
curl -X GET http://localhost:8080/api/v1/health \
  -H "Accept: application/json"

# Get statistics
curl -X GET http://localhost:8080/api/v1/stats \
  -H "Accept: application/json"
```

---

## Troubleshooting

### Common Issues

1. **Connection Refused**
   ```
   curl: (7) Failed to connect to localhost:8080
   ```
   Solution: Make sure the service is running on port 8080

2. **CSR ID Not Found**
   ```json
   {
     "error": "Not Found",
     "message": "CSR not found"
   }
   ```
   Solution: Verify the CSR ID is correct and hasn't been deleted

3. **Invalid JSON Response**
   Solution: Add `-H "Accept: application/json"` header to curl commands

4. **Keystore Password Issues**
   - Ensure password is at least 8 characters
   - Special characters should be URL-encoded in bash scripts

---

## Performance Tips

1. **Batch Operations**: Generate multiple CSRs/certificates in parallel
2. **Pagination**: Use pagination for listing to improve performance
3. **Caching**: Cache certificate details locally when possible
4. **Connection Pooling**: Reuse HTTP connections in Java clients

---

## Security Considerations

1. **Never log passwords** in production
2. **Use HTTPS** instead of HTTP in production
3. **Protect private keys** with appropriate file permissions
4. **Rotate keystores** regularly
5. **Implement API authentication** before production use

---

For more information, see [API Documentation](API.md)
