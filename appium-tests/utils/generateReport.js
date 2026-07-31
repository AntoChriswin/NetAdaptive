const ExcelJS = require('exceljs');
const path = require('path');
const fs = require('fs');

async function generateReport(testResults) {
    const workbook = new ExcelJS.Workbook();

    // Sheet 1: Summary
    const summarySheet = workbook.addWorksheet('Summary');
    summarySheet.columns = [
        { header: 'Metric', key: 'metric', width: 30 },
        { header: 'Value', key: 'value', width: 30 }
    ];

    const total = testResults.length;
    const passed = testResults.filter(r => r.status === 'PASS').length;
    const failed = testResults.filter(r => r.status === 'FAIL').length;
    const blocked = testResults.filter(r => r.status === 'BLOCKED').length;
    const notRun = testResults.filter(r => r.status === 'NOT RUN').length;

    summarySheet.addRows([
        { metric: 'Project Name', value: 'NetAdaptive' },
        { metric: 'Execution Date', value: new Date().toLocaleDateString() },
        { metric: 'Total Test Cases', value: total },
        { metric: 'Passed', value: passed },
        { metric: 'Failed', value: failed },
        { metric: 'Blocked', value: blocked },
        { metric: 'Not Run', value: notRun },
        { metric: 'Pass Percentage', value: total > 0 ? `${((passed / total) * 100).toFixed(2)}%` : '0%' }
    ]);

    // Sheet 2: Test Case Details
    const detailsSheet = workbook.addWorksheet('Test Case Details');
    detailsSheet.columns = [
        { header: 'Test Case ID', key: 'id', width: 15 },
        { header: 'Test Case Name', key: 'name', width: 40 },
        { header: 'Status', key: 'status', width: 10 },
        { header: 'Duration (ms)', key: 'duration', width: 15 },
        { header: 'Timestamp', key: 'timestamp', width: 25 },
        { header: 'Error Message', key: 'error', width: 50 },
        { header: 'Screenshot', key: 'screenshot', width: 50 }
    ];

    testResults.forEach(result => {
        detailsSheet.addRow(result);
    });

    // Formatting
    [summarySheet, detailsSheet].forEach(sheet => {
        sheet.getRow(1).font = { bold: true };
        sheet.getRow(1).fill = {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: 'FFE0E0E0' }
        };
    });

    const reportPath = path.join(__dirname, '../reports/appium-test-report.xlsx');
    await workbook.xlsx.writeFile(reportPath);
    console.log(`Report generated at: ${reportPath}`);
}

module.exports = generateReport;
