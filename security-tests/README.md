# NetAdaptive Security Testing Framework

This framework provides automated security analysis for the NetAdaptive mobile application and its backend interactions.

## Scope
- Static Application Security Testing (SAST) for Kotlin/XML code.
- Secret Scanning (API keys, tokens).
- Dependency Vulnerability Analysis.
- Backend & API Inventory Discovery.

## Folder Structure
- `scripts/`: Python/Node.js scripts for running scans and generating reports.
- `raw-results/`: JSON outputs from individual scanners.
- `reports/`: Human-readable Markdown and Excel reports.
- `tests/`: Placeholders for manual security test cases (Auth, Authz, etc.).

## Prerequisites
- Node.js installed.
- `npm install` inside this folder.

## Execution

Run all security checks and generate reports:
```bash
npm run test:all
```

Individual scans:
- **Secrets**: `npm run scan:secrets`
- **SAST**: `npm run scan:sast`
- **Dependencies**: `npm run scan:dependencies`
- **Generate Report**: `npm run report`

## Methodology
- **Severity**: Critical, High, Medium, Low, Informational.
- **Scoring**: Base score of 100, deducted for each finding based on severity.

## Safe DAST
Dynamic testing is disabled by default. To enable, configure `TARGET_URL` and `ENABLE_DAST=true` in a `.env` file. Only perform dynamic testing against authorized environments.

## Reports
- `reports/executive-summary.md`: High-level overview.
- `reports/security-review.md`: Detailed technical findings.
- `reports/findings.xlsx`: Tabular data for tracking remediation.
