const fs = require('fs');
const path = require('path');

const libsFile = path.join(__dirname, '../../gradle/libs.versions.toml');
const results = [];

if (fs.existsSync(libsFile)) {
    const content = fs.readFileSync(libsFile, 'utf8');

    // Example vulnerable versions check
    const checks = [
        { name: 'Generative AI', current: /generativeai = "0\.9\.0"/, recommended: '0.12.0', severity: 'Medium', cve: 'Potential Version Risk' }
    ];

    checks.forEach(c => {
        if (c.current.test(content)) {
            results.push({
                package: c.name,
                installed: '0.9.0',
                severity: c.severity,
                cve: c.cve,
                fixed: c.recommended,
                scanner: 'Dependency Scanner'
            });
        }
    });
}

const outputPath = path.join(__dirname, '../raw-results/dependency-results.json');
fs.writeFileSync(outputPath, JSON.stringify(results, null, 2));
console.log(`Dependency scan completed. Found ${results.length} issues.`);
