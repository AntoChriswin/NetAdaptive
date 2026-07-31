const LoginPage = require('../pages/LoginPage');
const DashboardPage = require('../pages/DashboardPage');
const { expect } = require('chai');

describe('Smoke Tests', () => {
    it('APP-E2E-001: Should launch the app and show login screen', async () => {
        expect(await LoginPage.isDisplayed(LoginPage.emailField)).to.be.true;
    });

    it('APP-E2E-002: Should navigate to Signup screen', async () => {
        await LoginPage.clickSignup();
        // Add SignupPage and check here
    });

    it('APP-E2E-003: Should navigate to Forgot Password screen', async () => {
        await browser.back();
        await LoginPage.clickForgotPassword();
        // Add ForgotPasswordPage and check here
    });
});
