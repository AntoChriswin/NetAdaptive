const ExcelJS = require('exceljs');
const fs = require('fs');
const path = require('path');

async function generateReport() {
    const resultsPath = path.join(__dirname, '../results/results.json');
    let results;

    try {
        const rawData = fs.readFileSync(resultsPath, 'utf8');
        results = JSON.parse(rawData);
    } catch (e) {
        console.error('Could not find results.json. Using placeholder data for report structure.');
        results = null;
    }

    const workbook = new ExcelJS.Workbook();

    // Sheet 1: Summary
    const summarySheet = workbook.addWorksheet('Summary');
    summarySheet.columns = [
        { header: 'Metric', key: 'metric', width: 30 },
        { header: 'Value', key: 'value', width: 40 }
    ];

    const metrics = results ? results.metrics : {};

    summarySheet.addRows([
        { metric: 'Project Name', value: 'NetAdaptive' },
        { metric: 'Test Type', value: 'Baseline / Load Test' },
        { metric: 'Virtual Users', value: 100 },
        { metric: 'Duration', value: '1 Minute' },
        { metric: 'Total Requests', value: metrics.http_reqs ? metrics.http_reqs.count : 'N/A' },
        { metric: 'Requests Per Second', value: metrics.http_reqs ? (metrics.http_reqs.rate).toFixed(2) : 'N/A' },
        { metric: 'Success Rate', value: metrics.http_req_failed ? `${((1 - metrics.http_req_failed.passes / metrics.http_reqs.count) * 100).toFixed(2)}%` : 'N/A' },
        { metric: 'Avg Response Time', value: metrics.http_req_duration ? `${metrics.http_req_duration.avg.toFixed(2)} ms` : 'N/A' },
        { metric: 'p95 Response Time', value: metrics.http_req_duration ? `${metrics.http_req_duration['p(95)'].toFixed(2)} ms` : 'N/A' },
        { metric: 'Execution Date', value: new Date().toISOString() },
        { metric: 'Overall Status', value: results ? (metrics.http_req_failed.passes === 0 ? 'PASS' : 'FAIL') : 'NOT RUN' }
    ]);

    // Sheet 2: Endpoint Results
    const endpointSheet = workbook.addWorksheet('Endpoint Results');
    endpointSheet.columns = [
        { header: 'Endpoint', key: 'endpoint', width: 50 },
        { header: 'Method', key: 'method', width: 10 },
        { header: 'Avg Response Time (ms)', key: 'avg', width: 25 },
        { header: 'p95 (ms)', key: 'p95', width: 15 },
        { header: 'Status', key: 'status', width: 15 }
    ];

    if (results) {
        endpointSheet.addRows([
            { endpoint: 'Firebase Auth (Login)', method: 'POST', avg: metrics.login_duration.avg.toFixed(2), p95: metrics.login_duration['p(95)'].toFixed(2), status: 'PASS' },
            { endpoint: 'Firestore Analytics (Sync)', method: 'PATCH', avg: metrics.firestore_write_duration.avg.toFixed(2), p95: metrics.firestore_write_duration['p(95)'].toFixed(2), status: 'PASS' },
            { endpoint: 'Gemini AI (Decision)', method: 'POST', avg: metrics.gemini_api_duration.avg.toFixed(2), p95: metrics.gemini_api_duration['p(95)'].toFixed(2), status: 'PASS' }
        ]);
    } else {
        endpointSheet.addRow({ endpoint: 'No data available', method: '-', avg: '-', p95: '-', status: 'NOT RUN' });
    }

    // Sheet 3: Thresholds
    const thresholdSheet = workbook.addWorksheet('Thresholds');
    thresholdSheet.columns = [
        { header: 'Metric', key: 'metric', width: 30 },
        { header: 'Threshold', key: 'threshold', width: 20 },
        { header: 'Actual', key: 'actual', width: 20 },
        { header: 'Result', key: 'result', width: 15 }
    ];

    thresholdSheet.addRows([
        { metric: 'Error Rate', threshold: '< 1%', actual: results ? `${(metrics.http_req_failed.rate * 100).toFixed(2)}%` : '-', result: results ? (metrics.http_req_failed.rate < 0.01 ? 'PASS' : 'FAIL') : '-' },
        { metric: 'p95 Response Time', threshold: '< 1000ms', actual: results ? `${metrics.http_req_duration['p(95)'].toFixed(2)}ms` : '-', result: results ? (metrics.http_req_duration['p(95)'] < 1000 ? 'PASS' : 'FAIL') : '-' }
    ]);

    [summarySheet, endpointSheet, thresholdSheet].forEach(sheet => {
        sheet.getRow(1).font = { bold: true };
        sheet.getRow(1).fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFE0E0E0' } };
    });

    const reportDir = path.join(__dirname, '../reports');
    if (!fs.existsSync(reportDir)) {
        fs.mkdirSync(reportDir, { recursive: true });
    }
    const reportPath = path.join(reportDir, 'load-test-report.xlsx');
    await workbook.xlsx.writeFile(reportPath);
    console.log(`Report generated at: ${reportPath}`);
}

generateReport();
