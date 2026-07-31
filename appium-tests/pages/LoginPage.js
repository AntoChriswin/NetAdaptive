const BasePage = require('./BasePage');

class LoginPage extends BasePage {
    get emailField() { return 'id=com.simats.netadaptive:id/etEmail'; }
    get passwordField() { return 'id=com.simats.netadaptive:id/etPassword'; }
    get loginButton() { return 'id=com.simats.netadaptive:id/btnLogin'; }
    get googleSignInButton() { return 'id=com.simats.netadaptive:id/btnGoogleSignIn'; }
    get forgotPasswordLink() { return 'id=com.simats.netadaptive:id/tvForgotPassword'; }
    get signupLink() { return 'id=com.simats.netadaptive:id/tvSignup'; }
    get progressBar() { return 'id=com.simats.netadaptive:id/progressBar'; }

    async login(email, password) {
        await this.type(this.emailField, email);
        await this.type(this.passwordField, password);
        await this.click(this.loginButton);
    }

    async clickGoogleSignIn() {
        await this.click(this.googleSignInButton);
    }

    async clickForgotPassword() {
        await this.click(this.forgotPasswordLink);
    }

    async clickSignup() {
        await this.click(this.signupLink);
    }
}

module.exports = new LoginPage();
