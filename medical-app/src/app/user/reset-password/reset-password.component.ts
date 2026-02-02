import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm!: FormGroup;
  showAlert = false;
  alertMsg = '';
  alertColor = 'blue';
  inSubmission = false;
  passwordReset = false;
  tokenValid = false;
  tokenChecking = true;
  token = '';

  // Password visibility
  showPassword = false;
  showConfirmPassword = false;

  // Password strength
  passwordStrength = 0;
  strengthLabel = '';
  strengthColor = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.resetPasswordForm = this.fb.group({
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });

    // Get token from URL
    this.token = this.route.snapshot.paramMap.get('token') || '';

    if (!this.token) {
      this.tokenChecking = false;
      this.tokenValid = false;
      return;
    }

    // Validate token
    this.authService.validateResetToken(this.token).subscribe({
      next: (response) => {
        this.tokenChecking = false;
        this.tokenValid = response.valid;
      },
      error: () => {
        this.tokenChecking = false;
        this.tokenValid = false;
      }
    });

    // Watch password changes for strength indicator
    this.resetPasswordForm.get('password')?.valueChanges.subscribe(password => {
      this.calculatePasswordStrength(password);
    });
  }

  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (password !== confirmPassword) {
      return { passwordMismatch: true };
    }
    return null;
  }

  calculatePasswordStrength(password: string): void {
    if (!password) {
      this.passwordStrength = 0;
      this.strengthLabel = '';
      this.strengthColor = '';
      return;
    }

    let strength = 0;

    // Length check
    if (password.length >= 8) strength += 20;
    if (password.length >= 12) strength += 10;

    // Character type checks
    if (/[a-z]/.test(password)) strength += 15;
    if (/[A-Z]/.test(password)) strength += 15;
    if (/\d/.test(password)) strength += 15;
    if (/[@$!%*?&]/.test(password)) strength += 15;

    // Variety bonus
    if (/[a-z]/.test(password) && /[A-Z]/.test(password) && /\d/.test(password) && /[@$!%*?&]/.test(password)) {
      strength += 10;
    }

    this.passwordStrength = Math.min(strength, 100);

    if (this.passwordStrength < 40) {
      this.strengthLabel = 'Weak';
      this.strengthColor = 'bg-red-500';
    } else if (this.passwordStrength < 70) {
      this.strengthLabel = 'Medium';
      this.strengthColor = 'bg-yellow-500';
    } else if (this.passwordStrength < 90) {
      this.strengthLabel = 'Strong';
      this.strengthColor = 'bg-green-500';
    } else {
      this.strengthLabel = 'Very Strong';
      this.strengthColor = 'bg-green-600';
    }
  }

  togglePasswordVisibility(field: string): void {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  async onSubmit(): Promise<void> {
    if (this.resetPasswordForm.invalid) {
      return;
    }

    this.showAlert = true;
    this.alertMsg = 'Resetting your password...';
    this.alertColor = 'blue';
    this.inSubmission = true;

    const { password } = this.resetPasswordForm.value;

    this.authService.resetPassword(this.token, password).subscribe({
      next: (response) => {
        this.inSubmission = false;
        this.passwordReset = true;
        this.alertMsg = response.message;
        this.alertColor = 'green';
      },
      error: (error) => {
        this.inSubmission = false;
        this.alertMsg = error?.message || 'Failed to reset password. Please try again.';
        this.alertColor = 'red';
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/']);
  }

  requestNewLink(): void {
    this.router.navigate(['/forgot-password']);
  }

  // Password requirement checkers for template
  get passwordValue(): string {
    return this.resetPasswordForm.get('password')?.value || '';
  }

  hasMinLength(): boolean {
    return this.passwordValue.length >= 8;
  }

  hasLowercase(): boolean {
    return /[a-z]/.test(this.passwordValue);
  }

  hasUppercase(): boolean {
    return /[A-Z]/.test(this.passwordValue);
  }

  hasNumber(): boolean {
    return /\d/.test(this.passwordValue);
  }

  hasSpecialChar(): boolean {
    return /[$!%*?&]/.test(this.passwordValue);
  }
}
