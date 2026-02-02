import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, throwError, BehaviorSubject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Doctor } from '../models/doctor.model';
import { Patient } from '../models/patient.model';
import { IUser } from '../models/user.model';
import { RoleService } from './role.service';

// Response interfaces
export interface ApiResponse {
  status: string;
  message: string;
  code?: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  message?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseAuthUrl = 'http://localhost:8080/api/auth';
  private baseUserUrl = 'http://localhost:8080/api/user';

  // 1) Holds the current user object in memory
  private currentUser: IUser | null = null;

  private selectedRole: string = '';

  // 2) Expose user changes as a BehaviorSubject
  private userSubject = new BehaviorSubject<IUser | null>(null);
  public user$ = this.userSubject.asObservable();

  // For checking if user is logged in (e.g. for hiding modals)
  public isAuthenticated$: Observable<boolean>;

  constructor(
    private http: HttpClient,
    private roleService: RoleService
  ) {
    // isAuthenticated$ is based on whether 'token' is in localStorage
    this.isAuthenticated$ = of(!!localStorage.getItem('token')).pipe(
      map(token => !!token)
    );

    this.roleService.selectedRole$.subscribe((newRole) => {
      this.selectedRole = newRole;
    });

    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
      // Push that user into the BehaviorSubject
      this.userSubject.next(this.currentUser);
    }
  }

  /**
   * Register new user -> calls either /register/patient or /register/doctor
   */
  public async createUser(userData: any): Promise<void> {
    let endpoint = `${this.baseAuthUrl}/register/patient`;
    if (userData.role === 'DOCTOR' || userData.role === 'DOCTOR') {
      endpoint = `${this.baseAuthUrl}/register/doctor`;
    }

    const response = await this.http
      .post(endpoint, userData, { responseType: 'text' })
      .pipe(catchError(this.handleError))
      .toPromise();

    if (!response || response.includes('failed')) {
      throw new Error(response || 'Unknown registration error');
    }
    console.log('Registration Success:', response);
  }

  /**
   * Login -> get JWT -> store token -> find user role -> push user into BehaviorSubject
   */
  public async login(email: string, password: string, rememberMe: boolean = false): Promise<void> {
    const payload = { username: email, password, role: this.selectedRole, rememberMe };
    console.log('Login payload:', payload);

    // 1) Get token from /auth/login
    const token = await this.http
      .post(`${this.baseAuthUrl}/login`, payload, { responseType: 'text' })
      .pipe(catchError(this.handleError))
      .toPromise();

    if (!token) {
      throw new Error('No token returned from server');
    }
    localStorage.setItem('token', token);
    console.log('Logged in - JWT stored in localStorage:', token);

    // 2) Determine if the user is a doctor or patient
    const userRole = await this.determineRoleByEmail(email);

    // 3) Build user object, store it in memory + localStorage
    this.currentUser = { email, password, role: userRole };
    localStorage.setItem('user', JSON.stringify(this.currentUser));

    // 4) Push the new user into the BehaviorSubject => nav sees immediate update
    this.userSubject.next(this.currentUser);

    console.log('User logged in with role:', userRole);
  }

  /**
   * Call /user/doctors and /user/patients to see which list has 'email'.
   */
  private async determineRoleByEmail(email: string): Promise<string> {
    try {
      // get all doctors
      const doctors = await this.http
        .get<Doctor[]>(`${this.baseUserUrl}/doctors`)
        .pipe(catchError(this.handleError))
        .toPromise();
      if (doctors && doctors.some(d => d.email === email)) {
        return 'DOCTOR';
      }

      // if not doctor, check patients
      const patients = await this.http
        .get<Patient[]>(`${this.baseUserUrl}/patients`)
        .pipe(catchError(this.handleError))
        .toPromise();
      if (patients && patients.some(p => p.email === email)) {
        return 'PATIENT';
      }

      return 'UNKNOWN';
    } catch (err) {
      console.error('Error determining role from /user/doctors or /user/patients:', err);
      return 'UNKNOWN';
    }
  }

  /**
   * isDoctor() / isPatient() can remain the same if you want,
   * but the Navbar could just subscribe to user$. 
   */
  public isDoctor(): boolean {
    return this.currentUser?.role?.toUpperCase() === 'DOCTOR';
  }

  public isPatient(): boolean {
    return this.currentUser?.role?.toUpperCase() === 'PATIENT';
  }

  private getCurrentUser(): IUser | null {
    // If null in memory, try localStorage
    if (!this.currentUser) {
      const userStr = localStorage.getItem('user');
      this.currentUser = userStr ? JSON.parse(userStr) : null;
    }
    return this.currentUser;
  }

  /**
   * Logout -> remove token & user from localStorage -> userSubject.next(null)
   */
  public async logout($event?: Event): Promise<void> {
    if ($event) {
      $event.preventDefault();
    }
  
    try {
      // If the server returns plain text or an empty body,
      // specify { responseType: 'text' } so Angular won't parse it as JSON.
      await this.http.post(
        `${this.baseAuthUrl}/logout`, 
        null, 
        { responseType: 'text' }
      ).toPromise();
  
      console.log('Server-side logout successful.');
    } catch (error) {
      console.error('Error calling /auth/logout:', error);
    }
  
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUser = null;
    this.userSubject.next(null);
  
    console.log('Logged out locally (token removed).');
  }  

  // ==================== PASSWORD RESET ====================

  /**
   * Request password reset email
   */
  public forgotPassword(email: string, role: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.baseAuthUrl}/forgot-password`, { email, role })
      .pipe(catchError(this.handleError));
  }

  /**
   * Validate reset token before showing reset form
   */
  public validateResetToken(token: string): Observable<TokenValidationResponse> {
    return this.http.get<TokenValidationResponse>(`${this.baseAuthUrl}/validate-reset-token/${token}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Reset password with token
   */
  public resetPassword(token: string, newPassword: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.baseAuthUrl}/reset-password`, { token, newPassword })
      .pipe(catchError(this.handleError));
  }

  // ==================== EMAIL VERIFICATION ====================

  /**
   * Verify email with token
   */
  public verifyEmail(token: string): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(`${this.baseAuthUrl}/verify-email/${token}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Resend verification email
   */
  public resendVerificationEmail(email: string, role: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.baseAuthUrl}/resend-verification`, { email, role })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      console.error('Client-side error:', error.error.message);
    } else {
      console.error(`Backend error (status: ${error.status}):`, error.error);
    }
    return throwError(error.error || 'Server Error');
  }
}
