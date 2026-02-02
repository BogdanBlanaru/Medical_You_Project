import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  FormGroup,
  Validators,
  FormBuilder
} from '@angular/forms';
import { AuthService } from 'src/app/services/auth.service';
import { Subscription } from 'rxjs';
import { EmailTaken } from '../validators/email-taken';
import { RegisterValidators } from '../validators/register-validators';
import { Role } from 'src/app/models/role.enum';
import { RoleService } from 'src/app/services/role.service';


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit, OnDestroy {
  inSubmission = false;
  showAlert = false;
  alertMsg = 'Please wait! Your account is being created.';
  alertColor = 'blue';

  // Password strength meter
  passwordStrength: {
    score: number;
    label: string;
    color: string;
    barWidth: string;
    checks: {
      length: boolean;
      uppercase: boolean;
      lowercase: boolean;
      number: boolean;
      special: boolean;
    };
  } = {
    score: 0,
    label: '',
    color: 'bg-gray-200',
    barWidth: '0%',
    checks: {
      length: false,
      uppercase: false,
      lowercase: false,
      number: false,
      special: false
    }
  };

  // We'll pretend the user picks the role externally (e.g., via a roleService or a radio button).
  // For demonstration, we'll just store it here:
  selectedRole: string = 'PATIENT';

  // Toggle advanced fields for doctor
  doctorFieldsOpen = false;

  // The FormGroup we'll build
  registerForm!: FormGroup;

  // If you subscribe to a service for the role, store subscription here:
  private roleSubscription!: Subscription;

  constructor(
    private auth: AuthService,
    private roleService: RoleService,
    private emailTaken: EmailTaken,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.roleSubscription = this.roleService.selectedRole$.subscribe((newRole) => {
      // If the user picks a new role in the role modal, we update
      this.selectedRole = newRole;
      // Rebuild the form so the new fields (patient vs doctor) show up
      this.buildForm();
    });
  }

  /**
   * Build or rebuild the form based on selectedRole.
   */
  buildForm(): void {
    if (this.selectedRole === 'PATIENT') {
      // PATIENT form
      this.registerForm = this.fb.group(
        {
          name: [
            '',
            [Validators.required, Validators.minLength(3)],
          ],
          email: [
            '',
            {
              validators: [Validators.required, Validators.email],
              asyncValidators: [this.emailTaken.validate],
              updateOn: 'blur',
            },
          ],
          password: [
            '',
            [
              Validators.required,
              // Sample password pattern: min 8 chars, 1 uppercase, 1 lowercase, 1 digit
              Validators.pattern(/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/),
            ],
          ],
          confirm_password: ['', [Validators.required]],

          role: [Role.PATIENT],
        },
        {
          validators: [RegisterValidators.match('password', 'confirm_password')],
        }
      );
    } else {
      // DOCTOR form
      this.registerForm = this.fb.group(
        {
          name: [
            '',
            [Validators.required, Validators.minLength(3)],
          ],
          email: [
            '',
            {
              validators: [Validators.required, Validators.email],
              asyncValidators: [this.emailTaken.validate],
              updateOn: 'blur',
            },
          ],
          password: [
            '',
            [
              Validators.required,
              Validators.pattern(/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/),
            ],
          ],
          confirm_password: ['', [Validators.required]],
          role: [Role.DOCTOR],

          // DOCTOR-ONLY FIELDS with some basic validators:
          specialization: ['', [Validators.required, Validators.minLength(3)]],
          hospital: ['', [Validators.required, Validators.minLength(3)]],
          hospitalAddress: ['', [Validators.required]],
          rating: [
            null,
            [Validators.min(1), Validators.max(5)], // rating 1 to 5
          ],
          yearsOfExperience: [
            null,
            [Validators.min(0), Validators.max(50)], // 0 to 50 years
          ],
          education: ['', [Validators.required]],
          officeHours: ['', [Validators.required]],
          contactNumber: [
            '',
            [
              Validators.required,
              // just an example pattern: digits or digits with dashes
              Validators.pattern(/^[0-9-]+$/),
            ],
          ],
        },
        {
          validators: [RegisterValidators.match('password', 'confirm_password')],
        }
      );
    }
  }

  /**
   * Example method to toggle roles (could be triggered by a radio button or a role modal).
   */
  switchRole(newRole: 'PATIENT' | 'DOCTOR'): void {
    this.selectedRole = newRole;
    this.buildForm();
  }

  toggleDoctorFields(): void {
    this.doctorFieldsOpen = !this.doctorFieldsOpen;
  }

  /**
   * Calculate password strength based on various criteria
   */
  calculatePasswordStrength(password: string): void {
    if (!password) {
      this.passwordStrength = {
        score: 0,
        label: '',
        color: 'bg-gray-200',
        barWidth: '0%',
        checks: {
          length: false,
          uppercase: false,
          lowercase: false,
          number: false,
          special: false
        }
      };
      return;
    }

    const checks = {
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /\d/.test(password),
      special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };

    // Calculate score based on checks
    let score = 0;
    if (checks.length) score++;
    if (checks.uppercase) score++;
    if (checks.lowercase) score++;
    if (checks.number) score++;
    if (checks.special) score++;

    // Bonus for longer passwords
    if (password.length >= 12) score++;
    if (password.length >= 16) score++;

    // Determine label and color
    let label: string;
    let color: string;
    let barWidth: string;

    if (score <= 2) {
      label = 'Weak';
      color = 'bg-red-500';
      barWidth = '25%';
    } else if (score <= 4) {
      label = 'Fair';
      color = 'bg-yellow-500';
      barWidth = '50%';
    } else if (score <= 5) {
      label = 'Good';
      color = 'bg-blue-500';
      barWidth = '75%';
    } else {
      label = 'Strong';
      color = 'bg-green-500';
      barWidth = '100%';
    }

    this.passwordStrength = { score, label, color, barWidth, checks };
  }

  /**
   * Handle password input change for strength meter
   */
  onPasswordInput(event: Event): void {
    const password = (event.target as HTMLInputElement).value;
    this.calculatePasswordStrength(password);
  }

  async register(): Promise<void> {
    // Immediately show a loading alert
    this.showAlert = true;
    this.alertMsg = 'Please wait! Your account is being created.';
    this.alertColor = 'blue';
    this.inSubmission = true;

    // If form is invalid, stop
    if (this.registerForm.invalid) {
      this.alertMsg = 'Please correct all form errors before submitting.';
      this.alertColor = 'red';
      this.inSubmission = false;
      return;
    }

    try {
      console.log('Register Form Data:', this.registerForm.value);
      await this.auth.createUser(this.registerForm.value);
    } catch (e) {
      console.error(e);
      this.alertMsg = 'An unexpected error occurred. Please try again later.';
      this.alertColor = 'red';
      this.inSubmission = false;
      return;
    }

    this.alertMsg = 'Success! Your account has been created.';
    this.alertColor = 'green';
    this.inSubmission = false;

    this.registerForm.reset();
  }

  ngOnDestroy(): void {
    if (this.roleSubscription) {
      this.showAlert = false;
      this.roleSubscription.unsubscribe();
    }
  }
}
