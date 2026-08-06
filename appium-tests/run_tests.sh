#!/bin/bash
set -e

echo "[$(date)] Starting emulator diagnostics..."
adb devices

echo "[$(date)] Waiting for emulator boot (sys.boot_completed)..."
max_retries=30
counter=1
boot_completed=0

while [ $counter -le $max_retries ]; do
  # Get boot status and remove carriage return characters
  status=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  if [ "$status" = "1" ]; then
    echo "[$(date)] Emulator boot completed successfully!"
    boot_completed=1
    break
  fi
  echo "[$(date)] Still waiting for boot... ($counter/$max_retries)"
  sleep 10
  counter=$((counter + 1))
done

if [ "$boot_completed" -ne 1 ]; then
  echo "ERROR: Emulator failed to boot within 5 minutes."
  adb shell getprop init.svc.bootanim
  exit 1
fi

echo "[$(date)] Unlocking emulator screen..."
adb shell input keyevent 82

echo "[$(date)] Verifying APK existence..."
APK_PATH="../app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
  echo "ERROR: APK not found at $APK_PATH"
  # Try relative to root if the above fails (depends on where we are)
  APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
  if [ ! -f "$APK_PATH" ]; then
     echo "ERROR: APK not found at $APK_PATH either."
     exit 1
  fi
fi
ls -lh "$APK_PATH"

echo "[$(date)] Starting Appium server..."
# Use nohup and redirect to ensure it stays in background
nohup npx appium --log ../appium-server.log --address 127.0.0.1 --port 4723 --base-path / > ../appium-stdout.log 2>&1 &

echo "[$(date)] Waiting for Appium server to be ready..."
appium_ready=0
counter=1
while [ $counter -le 30 ]; do
  if curl -s http://127.0.0.1:4723/status | grep -q "\"ready\":true"; then
    echo "[$(date)] Appium server is ready and accepting requests!"
    appium_ready=1
    break
  fi
  echo "Waiting for Appium server status... ($counter/30)"
  sleep 2
  counter=$((counter + 1))
done

if [ "$appium_ready" -ne 1 ]; then
  echo "ERROR: Appium server failed to start within 60 seconds."
  cat ../appium-server.log
  exit 1
fi

echo "[$(date)] Running Appium Tests..."
# Run tests and capture exit code
TEST_EXIT_CODE=0
npm test || TEST_EXIT_CODE=$?

if [ $TEST_EXIT_CODE -ne 0 ]; then
  echo "Tests failed during execution with exit code $TEST_EXIT_CODE"
  TESTS_FAILED=true
fi

echo "[$(date)] Generating execution report..."
npm run report || echo "Report generation failed."

if [ "$TESTS_FAILED" = "true" ]; then
  exit 1
fi
