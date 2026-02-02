import { Injectable } from '@angular/core';
import {
  AsyncValidator,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';

@Injectable({
  providedIn: 'root',
})
export class EmailTaken implements AsyncValidator {
  constructor() {}

  validate(control: AbstractControl): Promise<ValidationErrors | null> {
    // Return a Promise to comply with AsyncValidator
    return new Promise((resolve) => {
      // Simulate a small delay (optional)
      setTimeout(() => {
        // Get a list of “registered” users from localStorage
        const usersJson = localStorage.getItem('users');
        const users = usersJson ? JSON.parse(usersJson) : [];

        // Check if the current email is already in that array
        const isTaken = users.some((u: any) => u.email === control.value);

        // If email is found, return an error object
        if (isTaken) {
          resolve({ emailTaken: true });
        } else {
          // Otherwise, null = valid
          resolve(null);
        }
      }, 500);
    });
  }
}
