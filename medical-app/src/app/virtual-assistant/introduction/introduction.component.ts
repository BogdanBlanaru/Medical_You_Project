import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-introduction',
  templateUrl: './introduction.component.html',
  styleUrls: ['./introduction.component.css'],
})
export class IntroductionComponent {
  currentSlide: 'welcome' | 'terms' = 'welcome'; // Track the current slide
  checkedConsent1 = false; // Track the first checkbox
  checkedConsent2 = false; // Track the second checkbox

  constructor(private router: Router) {}

  // Show the terms slide
  showTerms() {
    this.currentSlide = 'terms';
  }

  // Show the welcome slide
  showWelcome() {
    this.currentSlide = 'welcome';
  }

  // Navigate to the patient page
  goToPatientPage() {
    this.router.navigate(['/virtual-assistant/patient']);
  }
}
