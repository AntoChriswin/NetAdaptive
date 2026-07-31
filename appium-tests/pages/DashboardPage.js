const BasePage = require('./BasePage');

class DashboardPage extends BasePage {
    get homeTab() { return '//*[@text="Home"]'; }
    get networkTab() { return '//*[@text="Network"]'; }
    get appsTab() { return '//*[@text="Apps"]'; }
    get analyticsTab() { return '//*[@text="Analytics"]'; }
    get settingsTab() { return '//*[@text="Settings"]'; }

    get networkQualityHeader() { return '//*[@text="NETWORK QUALITY"]'; }
    get activeAppHeader() { return '//*[@text="ACTIVE APP"]'; }
    get backgroundAppsHeader() { return '//*[@text="BACKGROUND APPS"]'; }
    get sessionUsageHeader() { return '//*[@text="SESSION USAGE"]'; }

    async goToNetwork() {
        await this.click(this.networkTab);
    }

    async goToApps() {
        await this.click(this.appsTab);
    }

    async goToAnalytics() {
        await this.click(this.analyticsTab);
    }

    async goToSettings() {
        await this.click(this.settingsTab);
    }
}

module.exports = new DashboardPage();
