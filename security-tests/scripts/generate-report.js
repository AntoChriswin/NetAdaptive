const ExcelJS = require('exceljs');
const fs = require('fs');
const path = require('path');

async function generateReport() {
    const rawDir = path.join(__dirname, '../raw-results');
    const secretResults = JSON.parse(fs.readFileSync(path.join(rawDir, 'gitleaks-results.json'), 'utf8') || '[]');
    const sastResults = JSON.parse(fs.readFileSync(path.join(rawDir, 'semgrep-results.json'), 'utf8') || '[]');
    const dependencyResults = JSON.parse(fs.readFileSync(path.join(rawDir, 'dependency-results.json'), 'utf8') || '[]');

    const allFindings = [...secretResults, ...sastResults];

    const reportDir = path.join(__dirname, '../reports');
    if (!fs.existsSync(reportDir)) {
        fs.mkdirSync(reportDir, { recursive: true });
    }

    // 1. Executive Summary Markdown
    const execSummary = `
# Executive Summary - Security Review

Total Findings: ${allFindings.length + dependencyResults.length}

Critical: ${allFindings.filter(f => f.severity === 'Critical').length}
High: ${allFindings.filter(f => f.severity === 'High').length}
Medium: ${allFindings.filter(f => f.severity === 'Medium').length + dependencyResults.length}
Low: ${allFindings.filter(f => f.severity === 'Low').length}

Overall Security Score: ${Math.max(0, 100 - (allFindings.length * 5))} / 100

## Top Risks
${allFindings.slice(0, 3).map((f, i) => `${i+1}. ${f.title} (${f.severity})`).join('\n')}
    `;
    fs.writeFileSync(path.join(reportDir, 'executive-summary.md'), execSummary);

    // 2. Security Review Markdown
    let securityReview = '# Security Review Detailed Report\n\n';
    allFindings.forEach(f => {
        securityReview += `### ${f.id}: ${f.title}\n- **Severity**: ${f.severity}\n- **File**: ${f.file}\n- **Description**: ${f.description}\n- **Evidence**: ${f.evidence || 'N/A'}\n\n`;
    });
    fs.writeFileSync(path.join(reportDir, 'security-review.md'), securityReview);

    // 3. Excel Report - findings.xlsx
    const workbook = new ExcelJS.Workbook();

    // Sheet 1: Security Findings
    const findingsSheet = workbook.addWorksheet('Security Findings');
    findingsSheet.columns = [
        { header: 'Finding ID', key: 'id', width: 15 },
        { header: 'Title', key: 'title', width: 30 },
        { header: 'Severity', key: 'severity', width: 10 },
        { header: 'Confidence', key: 'confidence', width: 15 },
        { header: 'File Path', key: 'file', width: 50 },
        { header: 'Description', key: 'description', width: 50 },
        { header: 'Evidence', key: 'evidence', width: 30 }
    ];
    allFindings.forEach(f => findingsSheet.addRow(f));

    // Sheet 2: Endpoint Inventory
    const endpointSheet = workbook.addWorksheet('Endpoint Inventory');
    endpointSheet.columns = [
        { header: 'Endpoint', key: 'endpoint', width: 50 },
        { header: 'HTTP Method', key: 'method', width: 10 },
        { header: 'Auth Required', key: 'auth', width: 15 },
        { header: 'Security Relevance', key: 'relevance', width: 20 }
    ];
    const endpoints = [
        { endpoint: 'https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword', method: 'POST', auth: 'API Key', relevance: 'Login' },
        { endpoint: 'https://firestore.googleapis.com/v1/projects/netadaptive-bf351/databases/(default)/documents/users/{uid}', method: 'GET/PATCH', auth: 'JWT', relevance: 'User Data' },
        { endpoint: 'https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent', method: 'POST', auth: 'API Key', relevance: 'AI Logic' }
    ];
    endpoints.forEach(e => endpointSheet.addRow(e));

    // Sheet 3: Dependency Vulnerabilities
    const depSheet = workbook.addWorksheet('Dependency Vulnerabilities');
    depSheet.columns = [
        { header: 'Package', key: 'package', width: 20 },
        { header: 'Installed Version', key: 'installed', width: 15 },
        { header: 'CVE/Issue', key: 'cve', width: 25 },
        { header: 'Severity', key: 'severity', width: 10 },
        { header: 'Fixed Version', key: 'fixed', width: 15 }
    ];
    dependencyResults.forEach(d => depSheet.addRow(d));

    // Sheet 4: Risk Summary
    const riskSheet = workbook.addWorksheet('Risk Summary');
    riskSheet.addRow(['Metric', 'Count']);
    riskSheet.addRow(['Critical', allFindings.filter(f => f.severity === 'Critical').length]);
    riskSheet.addRow(['High', allFindings.filter(f => f.severity === 'High').length]);
    riskSheet.addRow(['Medium', allFindings.filter(f => f.severity === 'Medium').length + dependencyResults.length]);
    riskSheet.addRow(['Low', allFindings.filter(f => f.severity === 'Low').length]);
    riskSheet.addRow(['Security Score', Math.max(0, 100 - (allFindings.length * 5))]);

    // Sheet 5: Test Execution Summary
    const execSheet = workbook.addWorksheet('Test Execution Summary');
    execSheet.columns = [
        { header: 'Test Category', key: 'category', width: 25 },
        { header: 'Execution Status', key: 'status', width: 15 },
        { header: 'Findings', key: 'findings', width: 10 }
    ];
    execSheet.addRow({ category: 'Secret Scanning', status: 'COMPLETED', findings: secretResults.length });
    execSheet.addRow({ category: 'SAST', status: 'COMPLETED', findings: sastResults.length });
    execSheet.addRow({ category: 'Dependency Scanning', status: 'COMPLETED', findings: dependencyResults.length });
    execSheet.addRow({ category: 'DAST', status: 'NOT RUN', findings: 0 });

    await workbook.xlsx.writeFile(path.join(reportDir, 'findings.xlsx'));

    // 4. Separate Endpoint Inventory - endpoint-inventory.xlsx
    const epWorkbook = new ExcelJS.Workbook();
    const epSheet = epWorkbook.addWorksheet('Endpoints');
    epSheet.columns = endpointSheet.columns;
    endpoints.forEach(e => epSheet.addRow(e));
    await epWorkbook.xlsx.writeFile(path.join(reportDir, 'endpoint-inventory.xlsx'));

    console.log('Reports generated successfully.');
}

generateReport();
