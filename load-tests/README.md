# NetAdaptive Baseline / Load Testing Framework

This folder contains the performance testing suite for the NetAdaptive application backend (Firebase & Gemini AI).

## Overview

The goal of this framework is to establish a performance baseline for the critical services the application depends on. 

### Target Configuration
- **Virtual Users (VU)**: 100
- **Duration**: 60 seconds (1 minute steady state)
- **Tool**: [k6](https://k6.io/)

## Folder Structure

- `tests/`: Contains k6 test scripts.
- `scripts/`: Utilities for report generation.
- `results/`: Raw execution results (JSON).
- `reports/`: Human-readable Excel reports.
- `config/`: Configuration files and environment setups.

## Prerequisites

1. **k6**: [Install k6](https://k6.io/docs/getting-started/installation/)
2. **Node.js**: Required for report generation.
3. **npm install**: Run inside `load-tests/` to install report dependencies.

## Setup

1. Copy `.env.example` to `.env` (optional, k6 can take env vars).
2. Ensure you have the necessary API keys for Firebase and Gemini.

## Execution

### 1. Run Baseline Test
```bash
npm run test:baseline
```
This will execute the test with 100 concurrent users and save raw metrics to `results/results.json`.

### 2. Generate Excel Report
```bash
npm run report
```
This generates `reports/load-test-report.xlsx`.

## Performance Thresholds
- **Error Rate**: < 1%
- **p95 Response Time**: < 1000ms
- **Avg Response Time**: < 500ms

## Scenarios
1. **Authentication**: Simulates user login via Firebase Auth REST API.
2. **Analytics Sync**: Simulates retrieving and updating user analytics in Firestore.
3. **AI Priority Engine**: Simulates network priority categorization requests to Google Gemini AI.

## Safety Warnings
- Do NOT run high load against production environments without authorization.
- Use dedicated test accounts to avoid polluting real user data.
- Be mindful of Google Cloud and Firebase quotas.
