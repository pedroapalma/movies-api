# Quality Configuration

This directory contains configuration files for code quality and security analysis tools.

## Structure

```
.quality/
├── pmd/
│   └── ruleset.xml          # PMD static analysis rules
├── spotbugs/
│   ├── include-filter.xml   # SpotBugs inclusion rules
│   └── exclude-filter.xml   # SpotBugs exclusion rules
├── owasp/
│   └── suppressions.xml     # OWASP dependency check suppressions
└── README.md               # This file
```

## Tools Configuration

### PMD (Static Analysis)
- **File**: `pmd/ruleset.xml`
- **Purpose**: Detects code quality issues, potential bugs, and style violations
- **Configuration**: Balanced ruleset optimized for Spring Boot applications
- **Priority**: Only reports priority 1-2 violations (most important)

### SpotBugs (Bug Detection)
- **Files**: `spotbugs/include-filter.xml`, `spotbugs/exclude-filter.xml`
- **Purpose**: Finds potential bugs and security vulnerabilities in bytecode
- **Configuration**: Includes FindSecBugs for security analysis
- **Exclusions**: Common Spring Boot false positives excluded

### OWASP Dependency Check (Vulnerability Analysis)
- **File**: `owasp/suppressions.xml`
- **Purpose**: Scans dependencies for known security vulnerabilities
- **Configuration**: Fails build on CVSS 7+ vulnerabilities
- **Suppressions**: Test dependencies and dev-only libraries excluded

## Usage

These configurations are automatically used by Maven plugins:

```bash
# Run all quality checks
mvn verify

# Individual tools
mvn pmd:check
mvn spotbugs:check
mvn org.owasp:dependency-check-maven:check

# Format code
mvn spotless:apply
```

## Customization

To modify the rules:

1. Edit the appropriate XML file in this directory
2. Test your changes with `mvn verify`
3. Commit the updated configuration

## Reports

Quality reports are generated in:
- `target/pmd.xml` - PMD results
- `target/spotbugsXml.xml` - SpotBugs results  
- `target/dependency-check-report/` - OWASP reports