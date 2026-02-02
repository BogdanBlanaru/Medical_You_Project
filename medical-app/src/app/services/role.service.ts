import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  // We'll store the selected role in a BehaviorSubject
  // so we can get its last value and also subscribe to changes.
  private selectedRoleSubject = new BehaviorSubject<string>('patient');

  // Expose an observable for other components to subscribe to
  selectedRole$: Observable<string> = this.selectedRoleSubject.asObservable();

  // Method to change the role
  setSelectedRole(newRole: string) {
    this.selectedRoleSubject.next(newRole);
  }

  // Getter for immediate access
  getSelectedRole(): string {
    return this.selectedRoleSubject.getValue();
  }
}
