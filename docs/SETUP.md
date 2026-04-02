# Setup Guide

## Development Environment Setup

### Prerequisites

- **Java Development Kit (JDK) 17** or higher
  - Download: https://www.oracle.com/java/technologies/downloads/
  - Or use OpenJDK: `brew install openjdk@17` (macOS) or `apt install openjdk-17-jdk` (Linux)

- **Apache Maven 3.9.10** or higher
  - Download: https://maven.apache.org/download.cgi
  - Or install via package manager: `brew install maven` (macOS) or `apt install maven` (Linux)

- **Git** for version control
  - Download: https://git-scm.com/downloads

- **IDE** (Optional but recommended)
  - IntelliJ IDEA (Community/Ultimate)
  - Eclipse IDE
  - VS Code with Extension Pack for Java

### Verify Installation

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check Git version
git --version
```

---

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service.git
cd spring-boot-ssl-certificate-service
```

### 2. Build the Project

```bash
# Full clean build
mvn clean package

# Build without running tests
mvn clean package -DskipTests

# Quick build (no clean)
mvn package
```

**Expected Output:**
```
[INFO] Building jar: .../target/ssl-certificate-service-0.1.0.jar
[INFO] BUILD SUCCESS
```

### 3. Run the Application

#### Option 1: Maven Spring Boot Plugin (Development)
```bash
mvn spring-boot:run
```

#### Option 2: Run JAR directly (Production)
```bash
java -jar target/ssl-certificate-service-0.1.0.jar
```

#### Option 3: IDE Integration
- IntelliJ: Right-click `SslCertificateServiceApplication.java` → Run
- Eclipse: Right-click project → Run As → Spring Boot App
- VS Code: Use Spring Boot Dashboard extension

**Verification:**
```bash
# Check if service is running
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP","services":{"cryptography":"UP","storage":"UP","api":"UP"}}
```

---

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=CsrServiceTest
```

### Run with Coverage Report

```bash
mvn clean test jacoco:report

# Open the report
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

### Run with Debug

```bash
mvn test -Dmaven.surefire.debug
```

---

## Configuration

### Application Properties

File: `src/main/resources/application.properties`

**Common Configuration:**

```properties
# Server
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.root=INFO
logging.level.com.vignesh.ssl=DEBUG

# Storage
app.storage.path=certificates-storage

# Defaults
app.certificate.keysize.default=2048
app.certificate.validity.default=365
```

### Environment-Specific Configuration

Create `application-dev.properties` or `application-prod.properties`:

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'

# Production
mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=prod'
```

---

## IDE Configuration

### IntelliJ IDEA

1. **Open Project:**
   - File → Open → Select project directory
   - Accept "Load Gradle/Maven changes" prompt

2. **Configure JDK:**
   - File → Project Structure → Project → SDK → Select Java 17
   - File → Project Structure → Modules → Language Level → SDK default

3. **Run Application:**
   - Click Run button in toolbar
   - Or: Right-click `SslCertificateServiceApplication.java` → Run

4. **Run Tests:**
   - Right-click `src/test/java` → Run Tests
   - Or: Ctrl+Shift+F10 (Windows/Linux) / Cmd+Shift+R (macOS)

### VS Code

1. **Install Extensions:**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Maven for Java

2. **Open Project:**
   - File → Open Folder → Select project directory

3. **Run Application:**
   - Use Spring Boot Dashboard on left sidebar
   - Or: Run → Start Debugging

### Eclipse

1. **Import Project:**
   - File → Import → Maven → Existing Maven Projects
   - Select project root directory

2. **Configure JDK:**
   - Window → Preferences → Java → Installed JREs
   - Ensure Java 17 is selected

3. **Run Application:**
   - Right-click project → Run As → Spring Boot App

---

## Common Development Tasks

### View API Documentation

```
http://localhost:8080/swagger-ui.html
```

### Check Application Health

```bash
curl http://localhost:8080/actuator/health
```

### View Service Statistics

```bash
curl http://localhost:8080/api/v1/stats
```

### Generate Test CSR

```bash
curl -X POST http://localhost:8080/api/v1/csr/generate \
  -H "Content-Type: application/json" \
  -d '{
    "commonName": "test.local",
    "organization": "Test Org",
    "country": "AU",
    "state": "NSW",
    "locality": "Sydney",
    "keySize": 2048
  }'
```

### View Logs

```bash
# Follow logs (tail)
tail -f logs/application.log

# Or in real-time from console
mvn spring-boot:run
```

---

## Troubleshooting

### Build Fails: "JAVA_HOME not set"

**Solution:**
```bash
# Set JAVA_HOME
export JAVA_HOME=/path/to/java17
# On macOS with Homebrew:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Build Fails: "Maven not found"

**Solution:**
```bash
# Use Maven wrapper (included)
./mvnw clean package  # Linux/macOS
mvnw.cmd clean package  # Windows
```

### Application won't start: "Port 8080 already in use"

**Solution:**
```bash
# Change port
mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=8081'
```

### Tests fail with "Permission denied"

**Solution:**
```bash
# Fix permissions on certificates-storage
chmod -R 755 certificates-storage/
```

### Cannot find or load main class

**Solution:**
```bash
# Rebuild the project
mvn clean compile

# Check if SslCertificateServiceApplication exists
find . -name "*Application.java"
```

---

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/my-feature
```

### 2. Make Changes

Edit source files in `src/main/java/com/vignesh/ssl/`

### 3. Test Locally

```bash
mvn clean test
mvn spring-boot:run
```

### 4. Commit Changes

```bash
git add .
git commit -m "feat: add new feature"
```

### 5. Push Branch

```bash
git push origin feature/my-feature
```

### 6. Create Pull Request

On GitHub, create PR against `main` branch

---

## Useful Commands

```bash
# Clean and build
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Run specific test
mvn test -Dtest=CsrServiceTest#testGenerateCSR

# Generate coverage
mvn clean test jacoco:report

# Check dependencies
mvn dependency:tree

# Check outdated dependencies
mvn versions:display-dependency-updates

# Format code
mvn spotless:apply

# Run application with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'

# Debug on port 5005
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

---

## Next Steps

1. ✅ Set up development environment
2. ✅ Clone and build project
3. ✅ Run application locally
4. 👉 Read [API.md](API.md) for endpoint documentation
5. 👉 Check [EXAMPLES.md](EXAMPLES.md) for usage examples
6. 👉 Review [ARCHITECTURE.md](ARCHITECTURE.md) for system design

---

## Support

- GitHub Issues: https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service/issues
- Documentation: See `docs/` directory

