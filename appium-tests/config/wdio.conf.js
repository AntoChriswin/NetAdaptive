exports.config = {
    user: process.env.BROWSERSTACK_USERNAME || 'user',
    key: process.env.BROWSERSTACK_ACCESS_KEY || 'key',
    hostname: 'localhost',
    port: 4723,
    path: '/',
    specs: [
        '../tests/**/*.test.js'
    ],
    exclude: [],
    maxInstances: 1,
    capabilities: [{
        'appium:deviceName': process.env.ANDROID_DEVICE_NAME || 'emulator-5554',
        'appium:platformName': 'Android',
        'appium:platformVersion': process.env.ANDROID_PLATFORM_VERSION || '10.0',
        'appium:automationName': 'UiAutomator2',
        'appium:app': process.env.ANDROID_APP_PATH || '../app/build/outputs/apk/debug/app-debug.apk',
        'appium:appPackage': 'com.simats.netadaptive',
        'appium:appActivity': '.ui.onboarding.SplashScreenActivity',
        'appium:newCommandTimeout': 240,
        'appium:noReset': false,
        'appium:disableWindowAnimation': true,
        'appium:skipLogcatCapture': true,
        'appium:automationName': 'UiAutomator2'
    }],
    logLevel: 'info',
    bail: 0,
    baseUrl: 'http://localhost',
    waitforTimeout: 10000,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    services: [],
    framework: 'mocha',
    reporters: ['spec'],
    autoCompileOpts: {
        autoCompile: false
    },
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000
    },
    afterTest: async function(test, context, { error, result, duration, passed, retries }) {
        if (!passed) {
            const timestamp = new Date().getTime();
            const screenshotName = `ERROR_${test.title.replace(/\s+/g, '_')}_${timestamp}.png`;
            await browser.saveScreenshot(`./screenshots/${screenshotName}`);
            test.screenshotPath = `./screenshots/${screenshotName}`;
        }
        // Collect test data for reporting
        if (!global.testResults) global.testResults = [];
        global.testResults.push({
            id: test.id || `APP-E2E-${(global.testResults.length + 1).toString().padStart(3, '0')}`,
            name: test.title,
            duration: duration,
            status: passed ? 'PASS' : 'FAIL',
            error: error ? error.message : null,
            screenshot: test.screenshotPath || null,
            timestamp: new Date().toISOString()
        });
    },
    onComplete: async function(exitCode, config, capabilities, results) {
        // Generate Excel report here if needed, or via separate script
        const generateReport = require('../utils/generateReport');
        if (global.testResults && global.testResults.length > 0) {
            await generateReport(global.testResults);
        } else {
            console.log('No test results to report from onComplete.');
        }
    }
}
