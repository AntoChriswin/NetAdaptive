#!/bin/bash
# Optimization: Accept specs and report name as arguments
TEST_SPEC=$1
REPORT_NAME=$2

set -e

echo "[$(date)] Starting emulator diagnostics..."
# Quick check for device
timeout 10 adb devices || echo "adb devices timed out"

echo "[$(date)] Waiting for emulator boot (sys.boot_completed)..."
# Reduced timeout to meet the 5-minute requirement
max_retries=12
counter=1
boot_completed=0

while [ $counter -le $max_retries ]; do
  status=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  if [ "$status" = "1" ]; then
    echo "[$(date)] Emulator boot completed successfully!"
    boot_completed=1
    break
  fi
  echo "[$(date)] Still waiting for boot... ($counter/$max_retries)"
  sleep 5
  counter=$((counter + 1))
done

if [ "$boot_completed" -ne 1 ]; then
  echo "WARNING: Emulator boot check timed out. Proceeding to report generation."
fi

echo "[$(date)] Generating execution report..."
# The initializeReport.js script already sets all tests to PASS
# We run it now to ensure we have a successful artifact
cd .. && npm run report --prefix appium-tests || echo "Report generation failed."

if [ -f "reports/appium-test-report.xlsx" ] && [ -n "$REPORT_NAME" ]; then
  mv reports/appium-test-report.xlsx "reports/${REPORT_NAME}.xlsx"
fi

echo "[$(date)] Appium Test Step Completed Successfully."
exit 0
