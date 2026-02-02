import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { IUser } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class DoctorGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    // Similar logic: check user$. If user.role === 'DOCTOR', allow. Else redirect home.
    return this.authService.user$.pipe(
      take(1),
      map((user: IUser | null) => {
        const isDoctor = user?.role?.toUpperCase() === 'DOCTOR';

        if (!isDoctor) {
          console.log('DoctorGuard: Access denied. Redirecting to home.');
          this.router.navigate(['/']);
          return false;
        }

        console.log('DoctorGuard: Access granted for doctor.');
        return true;
      })
    );
  }
}
