import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
  verifying = true;
  verified = false;
  errorMessage = '';
  errorCode = '';

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');

    if (!token) {
      this.verifying = false;
      this.errorMessage = 'Invalid verification link.';
      this.errorCode = 'INVALID_TOKEN';
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (response) => {
        this.verifying = false;
        this.verified = true;
      },
      error: (error) => {
        this.verifying = false;
        this.verified = false;
        this.errorCode = error?.code || 'UNKNOWN_ERROR';
        this.errorMessage = error?.message || 'Failed to verify email. Please try again.';
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/']);
  }

  resendVerification(): void {
    this.router.navigate(['/resend-verification']);
  }
}
