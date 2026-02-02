import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { RoleService } from 'src/app/services/role.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm!: FormGroup;
  showAlert = false;
  alertMsg = '';
  alertColor = 'blue';
  inSubmission = false;
  emailSent = false;
  selectedRole = 'PATIENT';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private roleService: RoleService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      role: ['PATIENT', Validators.required]
    });

    this.roleService.selectedRole$.subscribe(role => {
      this.selectedRole = role || 'PATIENT';
      this.forgotPasswordForm.patchValue({ role: this.selectedRole });
    });
  }

  async onSubmit(): Promise<void> {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

    this.showAlert = true;
    this.alertMsg = 'Sending password reset email...';
    this.alertColor = 'blue';
    this.inSubmission = true;

    const { email, role } = this.forgotPasswordForm.value;

    this.authService.forgotPassword(email, role).subscribe({
      next: (response) => {
        this.inSubmission = false;
        this.emailSent = true;
        this.alertMsg = response.message;
        this.alertColor = 'green';
      },
      error: (error) => {
        this.inSubmission = false;
        this.alertMsg = error?.message || 'An error occurred. Please try again.';
        this.alertColor = 'red';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
