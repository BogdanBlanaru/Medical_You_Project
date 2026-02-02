import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthModalComponent } from './auth-modal/auth-modal.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { SharedModule } from '../shared/shared.module';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { RoleModalComponent } from './role-modal/role-modal.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { VerifyEmailComponent } from './verify-email/verify-email.component';
import { ResendVerificationComponent } from './resend-verification/resend-verification.component';
import { EmailTaken } from './validators/email-taken';

@NgModule({
  declarations: [
    AuthModalComponent,
    LoginComponent,
    RegisterComponent,
    RoleModalComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    VerifyEmailComponent,
    ResendVerificationComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    SharedModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [
    EmailTaken
  ],
  exports: [
    AuthModalComponent,
    RoleModalComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    VerifyEmailComponent,
    ResendVerificationComponent
  ]
})
export class UserModule { }
