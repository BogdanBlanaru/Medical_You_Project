import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { IUser } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class PacientGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    // We subscribe to user$.pipe(take(1)) to get the **latest** user (one-time)
    // Then we check if user.role === 'PATIENT'
    return this.authService.user$.pipe(
      take(1),  // complete the Observable after one emission
      map((user: IUser | null) => {
        const isPatient = user?.role?.toUpperCase() === 'PATIENT';

        if (!isPatient) {
          console.log('PacientGuard: Access denied. Redirecting to home.');
          this.router.navigate(['/']);
          return false;
        }

        console.log('PacientGuard: Access granted for patient.');
        return true;
      })
    );
  }
}
