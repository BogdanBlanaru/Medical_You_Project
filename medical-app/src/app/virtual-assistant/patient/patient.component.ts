import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-patient',
  templateUrl: './patient.component.html',
  styleUrls: ['./patient.component.css'],
})
export class PatientComponent {
  currentStep: 'gender' | 'age' = 'gender'; // Tracks the current step
  gender: string | null = null;
  age: number | null = null;

  constructor(private router: Router) {}

  // Method to handle gender selection
  selectGender(selectedGender: string): void {
    this.gender = selectedGender;
  }

  // Proceed to the age step
  proceedToAge(): void {
    if (this.gender) {
      this.currentStep = 'age';
    }
  }

  // Method to go back to gender selection
  goBackToGender(): void {
    this.currentStep = 'gender';
    this.gender = null; // Clear the gender selection if needed
  }

  // Validate the entered age
  isValidAge(): boolean {
    return this.age !== null && this.age > 0 && this.age <= 120;
  }

  // Proceed to the symptoms route
  proceedToSymptoms(): void {
    if (this.isValidAge()) {
      console.log('Gender:', this.gender, 'Age:', this.age); // Debugging output
      this.router.navigate(['/virtual-assistant/symptoms']);
    }
  }
}
