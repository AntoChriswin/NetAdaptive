const BasePage = require('./BasePage');

class SignupPage extends BasePage {
    get nameField() { return 'id=com.simats.netadaptive:id/etName'; } // Assuming ID
    get emailField() { return 'id=com.simats.netadaptive:id/etEmail'; }
    get passwordField() { return 'id=com.simats.netadaptive:id/etPassword'; }
    get confirmPasswordField() { return 'id=com.simats.netadaptive:id/etConfirmPassword'; }
    get signupButton() { return 'id=com.simats.netadaptive:id/btnSignup'; }
    get loginLink() { return 'id=com.simats.netadaptive:id/tvLogin'; }

    async signup(name, email, password, confirmPassword) {
        await this.type(this.nameField, name);
        await this.type(this.emailField, email);
        await this.type(this.passwordField, password);
        await this.type(this.confirmPasswordField, confirmPassword);
        await this.click(this.signupButton);
    }
}

module.exports = new SignupPage();
