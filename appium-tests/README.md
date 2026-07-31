# NetAdaptive Appium E2E Tests

This folder contains the Appium-based End-to-End (E2E) testing framework for the NetAdaptive Android application.

## Prerequisites

- Node.js (v16 or higher)
- Appium 2.x
- Android SDK
- Java Development Kit (JDK)
- Android Emulator or Physical Device
- UiAutomator2 Driver (`appium driver install uiautomator2`)

## Folder Structure

- `tests/`: Contains test scripts (organized by module).
- `pages/`: Page Object Model (POM) classes representing app screens.
- `utils/`: Reusable utilities (reporting, custom commands).
- `config/`: Appium and WebdriverIO configuration files.
- `reports/`: Generated test execution reports (Excel format).
- `screenshots/`: Screenshots captured on test failure.

## Setup

1. Install dependencies:
   ```bash
   cd appium-tests
   npm install
   ```

2. Configure environment variables (optional):
   - `ANDROID_DEVICE_NAME`: Name of your device (default: `emulator-5554`)
   - `ANDROID_PLATFORM_VERSION`: Android version (default: `11.0`)
   - `ANDROID_APP_PATH`: Path to the APK file (default: `./app/build/outputs/apk/debug/app-debug.apk`)

3. Start Appium Server:
   ```bash
   appium
   ```

## Running Tests

- Run all tests:
  ```bash
  npm test
  ```

- Run smoke tests:
  ```bash
  npm run test:smoke
  ```

- Run E2E tests:
  ```bash
  npm run test:e2e
  ```

- Generate report manually:
  ```bash
  npm run report
  ```

## Reports and Evidence

- **Excel Report**: After execution, a detailed report is generated at `appium-tests/reports/appium-test-report.xlsx`.
- **Screenshots**: Screenshots of failed tests are saved in `appium-tests/screenshots/`.

## Troubleshooting

- Ensure the device is recognized by adb: `adb devices`.
- Ensure the APK is built and available at the specified path.
- Check Appium logs for driver-related issues.
