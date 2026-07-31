const fs = require('fs');
const path = require('path');

const sastChecks = [
    {
        id: 'SEC-SAST-001',
        title: 'Insecure Logging (Log.d/Log.i)',
        severity: 'Low',
        regex: /Log\.[di]\(/g,
        description: 'Debug or Information logs may leak sensitive information in production builds.'
    },
    {
        id: 'SEC-SAST-002',
        title: 'Exported Activity without Permissions',
        severity: 'Medium',
        regex: /android:exported="true"/g,
        description: 'Activities exported to other apps should be protected with permissions to prevent unauthorized access.'
    },
    {
        id: 'SEC-SAST-003',
        title: 'Trusting All Certificates (Network Config)',
        severity: 'Critical',
        regex: /<trust-anchors>\s*<certificates src="system" \/>\s*<certificates src="user" \/>\s*<\/trust-anchors>/g,
        description: 'Trusting user-installed certificates can make the app vulnerable to MITM attacks.'
    }
];

const results = [];

function scanFile(filePath) {
    if (!filePath.endsWith('.kt') && !filePath.endsWith('.java') && !filePath.endsWith('.xml')) return;

    const content = fs.readFileSync(filePath, 'utf8');
    sastChecks.forEach(check => {
        if (check.regex.test(content)) {
            results.push({
                ...check,
                id: `${check.id}-${results.length + 1}`,
                file: filePath,
                confidence: 'Likely'
            });
        }
    });
}

function scanDirectory(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            if (!['node_modules', '.git', 'security-tests', 'load-tests', 'appium-tests'].includes(file)) {
                scanDirectory(fullPath);
            }
        } else {
            scanFile(fullPath);
        }
    }
}

const projectRoot = path.join(__dirname, '../../app/src/main');
scanDirectory(projectRoot);

const outputPath = path.join(__dirname, '../raw-results/semgrep-results.json');
fs.writeFileSync(outputPath, JSON.stringify(results, null, 2));
console.log(`SAST scan completed. Found ${results.length} issues.`);
