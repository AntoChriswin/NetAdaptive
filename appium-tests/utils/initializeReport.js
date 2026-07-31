const generateReport = require('./generateReport');
const fs = require('fs');
const path = require('path');

const allTests = [
    // Smoke
    { id: 'APP-E2E-001', name: 'Should launch the app and show login screen', module: 'Smoke' },
    { id: 'APP-E2E-002', name: 'Should navigate to Signup screen', module: 'Smoke' },
    { id: 'APP-E2E-003', name: 'Should navigate to Forgot Password screen', module: 'Smoke' },
];

// Helper to add tests from files
function parseTests(filePath, moduleName) {
    const content = fs.readFileSync(filePath, 'utf8');
    const regex = /it\('(APP-E2E-\d+): (.+?)',/g;
    let match;
    while ((match = regex.exec(content)) !== null) {
        allTests.push({
            id: match[1],
            name: match[2],
            module: moduleName
        });
    }
}

const testFiles = [
    { path: '../tests/auth_extended.test.js', module: 'Authentication' },
    { path: '../tests/dashboard_extended.test.js', module: 'Dashboard' },
    { path: '../tests/network_extended.test.js', module: 'Network' },
    { path: '../tests/apps_extended.test.js', module: 'Apps' },
    { path: '../tests/analytics_extended.test.js', module: 'Analytics' },
    { path: '../tests/settings_extended.test.js', module: 'Settings' },
    { path: '../tests/navigation_extended.test.js', module: 'Navigation' }
];

testFiles.forEach(f => {
    try {
        parseTests(path.join(__dirname, f.path), f.module);
    } catch (e) {
        console.error(`Error parsing ${f.path}: ${e.message}`);
    }
});

const testResults = allTests.map(t => ({
    id: t.id,
    name: t.name,
    module: t.module,
    status: 'NOT RUN',
    duration: 0,
    timestamp: new Date().toISOString(),
    error: 'Test environment not initialized for execution',
    screenshot: null
}));

generateReport(testResults).then(() => {
    console.log(`Initialized report with ${testResults.length} test cases.`);
});
