class BasePage {
    async waitForElement(selector, timeout = 10000) {
        const el = await $(selector);
        await el.waitForExist({ timeout });
        return el;
    }

    async click(selector) {
        const el = await this.waitForElement(selector);
        await el.click();
    }

    async type(selector, text) {
        const el = await this.waitForElement(selector);
        await el.setValue(text);
    }

    async getText(selector) {
        const el = await this.waitForElement(selector);
        return await el.getText();
    }

    async isDisplayed(selector) {
        try {
            const el = await $(selector);
            return await el.isDisplayed();
        } catch (e) {
            return false;
        }
    }
}

module.exports = BasePage;
