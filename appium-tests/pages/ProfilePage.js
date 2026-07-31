const BasePage = require('./BasePage');

class ProfilePage extends BasePage {
    get startVpnButton() { return '//*[@text="Start VPN"]'; }
    get stopVpnButton() { return '//*[@text="Stop VPN"]'; }
    get logoutButton() { return '//*[@text="Log Out"]'; }

    get displayName() { return '//*[@text="Display Name"]/following-sibling::*[1]'; }
    get email() { return '//*[@text="Email"]/following-sibling::*[1]'; }

    async logout() {
        await this.click(this.logoutButton);
    }

    async startVpn() {
        await this.click(this.startVpnButton);
    }

    async stopVpn() {
        await this.click(this.stopVpnButton);
    }
}

module.exports = new ProfilePage();
