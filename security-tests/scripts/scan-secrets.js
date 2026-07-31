const fs = require('fs');
const path = require('path');

const patterns = [
    { name: 'Google API Key', regex: /AIza[0-9A-Za-z\\-_]{35}/g },
    { name: 'Firebase Project ID', regex: /"project_id":\s*"([^"]+)"/g },
    { name: 'Generic Secret', regex: /const\s+\w*SECRET\w*\s*=\s*["']([^"']+)["']/gi },
    { name: 'Generic Password', regex: /const\s+\w*PASS\w*\s*=\s*["']([^"']+)["']/gi }
];

const results = [];

function scanDirectory(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            if (file !== 'node_modules' && file !== '.git' && file !== 'security-tests') {
                scanDirectory(fullPath);
            }
        } else {
            scanFile(fullPath);
        }
    }
}

function scanFile(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    patterns.forEach(p => {
        let match;
        while ((match = p.regex.exec(content)) !== null) {
            const redacted = match[0].substring(0, 8) + '****';
            results.push({
                id: `SEC-SECRET-${results.length + 1}`,
                title: `Hardcoded ${p.name} Detected`,
                severity: 'High',
                confidence: 'Confirmed',
                file: filePath,
                evidence: redacted,
                description: `A hardcoded ${p.name} was found in the source code. This can lead to unauthorized access if the APK is decompiled.`
            });
        }
    });
}

const projectRoot = path.join(__dirname, '../../');
scanDirectory(projectRoot);

const outputPath = path.join(__dirname, '../raw-results/gitleaks-results.json');
fs.writeFileSync(outputPath, JSON.stringify(results, null, 2));
console.log(`Secret scan completed. Found ${results.length} issues.`);
