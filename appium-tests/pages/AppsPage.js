const BasePage = require('./BasePage');

class AppsPage extends BasePage {
    get searchField() { return '//*[@text="Search apps..."]'; }
    get allFilter() { return '//*[@text="All"]'; }
    get foregroundFilter() { return '//*[@text="Foreground"]'; }
    get backgroundFilter() { return '//*[@text="Background"]'; }
    get highUsageFilter() { return '//*[@text="High usage"]'; }

    get highUsageHeader() { return '//*[@text="HIGH USAGE"]'; }
    get mediumUsageHeader() { return '//*[@text="MEDIUM USAGE"]'; }
    get lowUsageHeader() { return '//*[@text="LOW USAGE"]'; }

    get priorityRankingTeaser() { return '//*[@text="Priority ranking active"]'; }

    async searchApp(name) {
        await this.type(this.searchField, name);
    }

    async selectFilter(filterName) {
        await this.click(`//*[@text="${filterName}"]`);
    }
}

module.exports = new AppsPage();
