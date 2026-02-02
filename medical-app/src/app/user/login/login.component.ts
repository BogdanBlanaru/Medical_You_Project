import { Component } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { ModalService } from 'src/app/services/modal.service';
import { NgForm } from '@angular/forms';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  credentials = {
    email: '',
    password: '',
  };
  rememberMe = false;
  showAlert = false;
  alertMsg = 'Please wait! We are logging you in.';
  alertColor = 'blue';
  inSubmission = false;

  constructor(
    private auth: AuthService,
    private modal: ModalService
  ) {}

  async login(loginForm: NgForm) {
    this.showAlert = true;
    this.alertMsg = 'Please wait! We are logging you in.';
    this.alertColor = 'blue';
    this.inSubmission = true;

    try {
      await this.auth.login(this.credentials.email, this.credentials.password, this.rememberMe);
    } catch (e: any) {
      this.inSubmission = false;
      this.alertColor = 'red';

      // Check for specific error codes
      if (e?.code === 'EMAIL_NOT_VERIFIED') {
        this.alertMsg = 'Please verify your email before logging in. Check your inbox for the verification link.';
      } else {
        this.alertMsg = e?.message || 'Invalid email or password. Please try again.';
      }

      console.error(e);
      return;
    }

    this.alertMsg = 'Success! You are now logged in.';
    this.alertColor = 'green';
    this.inSubmission = false;

    loginForm.resetForm();

    // Close the "auth" modal
    setTimeout(() => {
      this.modal.toggleModal('auth');
      this.showAlert = false;
    }, 800);
  }

  closeModal(): void {
    this.modal.toggleModal('auth');
  }
}
