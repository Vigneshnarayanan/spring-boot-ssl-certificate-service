# Contributing to Spring Boot SSL Certificate Service

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to this project.

---

## 🎯 Code of Conduct

Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project, you agree to abide by its terms.

---

## 🐛 Reporting Bugs

### Before Submitting a Bug Report

1. Check the [GitHub Issues](https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service/issues) to see if the bug has already been reported.
2. Review the [troubleshooting guide](docs/SETUP.md#troubleshooting).
3. Check the [API documentation](docs/API.md) for clarification.

### How to Submit a Bug Report

When reporting a bug, please include:

- **Title:** Clear, descriptive summary
- **Description:** Detailed explanation of the bug
- **Steps to Reproduce:** Clear steps to reproduce the issue
- **Expected Behavior:** What you expected to happen
- **Actual Behavior:** What actually happened
- **Environment:**
  - Java version (`java -version`)
  - Maven version (`mvn -version`)
  - Operating System
  - Spring Boot version (from pom.xml)
- **Code Samples:** If applicable, code that demonstrates the issue
- **Error Messages:** Full error traces and logs

**Example:**
```
Title: CSR generation fails with RSA 4096-bit keys

Description:
When generating a CSR with RSA 4096-bit keys, the API returns a 500 error.

Steps to Reproduce:
1. POST to /api/v1/csr/generate
2. Set keySize to 4096
3. Observe 500 error

Expected: CSR should generate successfully
Actual: Returns error: "Unsupported key size"

Environment: Java 17, Maven 3.9.10, Spring Boot 4.0.3
```

---

## ✨ Suggesting Features

### Before Submitting a Feature Request

1. Check if the feature has been [suggested before](https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement).
2. Review the [Roadmap](README.md#-future-enhancements) to see planned features.
3. Consider if the feature aligns with the project's learning goals.

### How to Submit a Feature Request

Include the following information:

- **Title:** Clear description of the feature
- **Use Case:** Why this feature would be useful
- **Proposed Solution:** How you'd like it to work
- **Alternatives:** Other approaches you've considered
- **Additional Context:** Any other relevant information

**Example:**
```
Title: Add support for RSA key generation

Use Case: Users want to generate keys with different algorithms (RSA, ECDSA)

Proposed Solution: Add keyAlgorithm parameter to CSR generation endpoint

Example API:
POST /api/v1/csr/generate
{
  "keyAlgorithm": "RSA",
  "keySize": 2048
}
```

---

## 💻 Contributing Code

### Getting Started

1. Fork the repository
2. Clone your fork locally
3. Add upstream remote: `git remote add upstream https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service.git`
4. Create a feature branch: `git checkout -b feature/my-feature`

### Development Setup

See [SETUP.md](docs/SETUP.md) for detailed environment setup instructions.

### Making Changes

1. **Follow Code Style:**
   - Use 4 spaces for indentation
   - Follow Java naming conventions
   - Add Javadoc comments for public methods
   - Keep methods focused and single-responsibility

2. **Write Tests:**
   - Add unit tests for new functionality
   - Maintain or improve code coverage
   - Run tests locally before pushing: `mvn test`

3. **Update Documentation:**
   - Update relevant documentation files
   - Add code comments for complex logic
   - Update README if changing user-facing behavior

4. **Commit Messages:**
   - Use clear, descriptive commit messages
   - Format: `type: description`
   - Types: `feat`, `fix`, `docs`, `test`, `chore`, `refactor`
   - Example: `feat: add ECDSA key generation support`

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Example:**
```
feat(csr): add ECDSA key algorithm support

- Add keyAlgorithm parameter to CsrRequest
- Support RSA and ECDSA algorithms
- Update tests for new functionality

Closes #42
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

### Pull Request Process

1. **Update branch:**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Push to your fork:**
   ```bash
   git push origin feature/my-feature
   ```

3. **Create Pull Request:**
   - Fill out the PR template completely
   - Reference any related issues: `Closes #123`
   - Provide clear description of changes
   - Include screenshots or examples if applicable

4. **PR Template:**
   ```markdown
   ## Description
   Brief description of changes
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update
   
   ## Testing
   - [ ] Unit tests added
   - [ ] Tests pass locally
   - [ ] Manual testing completed
   
   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Documentation updated
   - [ ] No new warnings generated
   - [ ] Tests added/updated
   
   ## Related Issues
   Closes #123
   ```

### Code Review Process

- Maintainers will review your PR within a few days
- Address feedback and push updates
- CI/CD checks must pass before merging
- At least one approval required before merge

---

## 🧪 Testing Guidelines

### Write Tests For:

- All new public methods
- Bug fixes (add failing test, then fix)
- Edge cases and error conditions
- Integration between components

### Test Structure

```java
class CsrServiceTest {
    
    private CsrService csrService;
    
    @BeforeEach
    void setUp() {
        // Initialize services and mocks
    }
    
    @Test
    void testGenerateCSRSuccessfully() {
        // Arrange
        CsrRequest request = new CsrRequest(...);
        
        // Act
        CsrResponse response = csrService.generateCSR(request);
        
        // Assert
        assertNotNull(response.getCsrId());
        assertEquals("pending", response.getStatus());
    }
    
    @Test
    void testGenerateCSRWithInvalidInput() {
        // Should throw appropriate exception
        assertThrows(ValidationException.class, () -> {
            csrService.generateCSR(invalidRequest);
        });
    }
}
```

### Run Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=CsrServiceTest

# Specific test method
mvn test -Dtest=CsrServiceTest#testGenerateCSRSuccessfully

# With coverage
mvn clean test jacoco:report
```

---

## 📝 Documentation

### Update Documentation For:

- New APIs or endpoints
- Configuration changes
- New dependencies
- Breaking changes

### Documentation Files

- **README.md** - Project overview and quick start
- **docs/SETUP.md** - Environment setup
- **docs/API.md** - API reference
- **docs/EXAMPLES.md** - Usage examples
- **docs/ARCHITECTURE.md** - System design

---

## 🚀 Release Process

Maintainers handle releases with semantic versioning:

- **MAJOR:** Breaking changes
- **MINOR:** New features, backward compatible
- **PATCH:** Bug fixes, backward compatible

---

## 📋 Development Checklist

Before submitting a PR, ensure:

- [ ] Code follows project style guidelines
- [ ] Tests added for new functionality
- [ ] All tests pass: `mvn test`
- [ ] No new warnings: `mvn clean compile`
- [ ] Documentation updated
- [ ] Commit messages follow format
- [ ] No sensitive data in code
- [ ] Licensed under MIT (if adding dependencies)

---

## 🤝 Community

- **GitHub Issues:** Ask questions and report bugs
- **Discussions:** Share ideas and best practices
- **Wiki:** Share knowledge and tips

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Bouncy Castle Documentation](https://www.bouncycastle.org/)
- [Java Cryptography Documentation](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/security/package-summary.html)

---

## ❓ Questions?

- Check existing [GitHub Issues](https://github.com/Vigneshnarayanan/spring-boot-ssl-certificate-service/issues)
- Read the [documentation](docs/)
- Open a new discussion

---

## 🙏 Thank You!

Thank you for contributing to making this project better! Your efforts are greatly appreciated.

**Happy coding!** 🚀
