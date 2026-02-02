import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { RoleService } from 'src/app/services/role.service';

@Component({
  selector: 'app-resend-verification',
  templateUrl: './resend-verification.component.html'
})
export class ResendVerificationComponent implements OnInit {
  resendForm!: FormGroup;
  showAlert = false;
  alertMsg = '';
  alertColor = 'blue';
  inSubmission = false;
  emailSent = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private roleService: RoleService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.resendForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      role: ['PATIENT', Validators.required]
    });

    this.roleService.selectedRole$.subscribe(role => {
      if (role) {
        this.resendForm.patchValue({ role });
      }
    });
  }

  async onSubmit(): Promise<void> {
    if (this.resendForm.invalid) {
      return;
    }

    this.showAlert = true;
    this.alertMsg = 'Sending verification email...';
    this.alertColor = 'blue';
    this.inSubmission = true;

    const { email, role } = this.resendForm.value;

    this.authService.resendVerificationEmail(email, role).subscribe({
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
