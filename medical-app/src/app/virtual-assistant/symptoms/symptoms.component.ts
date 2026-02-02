import { Component, HostListener, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SymptomsService } from 'src/app/services/symptoms.service';
import { ChatService } from 'src/app/services/chat.service';
import { ResultsStateService } from 'src/app/services/results-state.service';

@Component({
  selector: 'app-symptoms',
  templateUrl: './symptoms.component.html'
})
export class SymptomsComponent implements OnInit {
  // Symptom dropdown properties
  selectedSymptoms: string[] = [];
  availableSymptoms: string[] = [];
  filteredSymptoms: string[] = [];
  searchTerm: string = '';
  dropdownOpen: boolean = false;

  // Chat properties
  chatMessages: { sender: string; message: string }[] = [];
  userMessage: string = '';
  userId: string = 'user123'; // Unique user ID
  chatCompleted: boolean = false; // To track chat completion
  predictionResult: any = null;

  constructor(
    private router: Router,
    private symptomsService: SymptomsService,
    private chatService: ChatService,
    private resultsStateService: ResultsStateService
  ) {}

  ngOnInit(): void {
    // Initialize symptoms
    this.symptomsService.getSymptoms().subscribe((symptoms) => {
      this.availableSymptoms = symptoms;
      this.filteredSymptoms = symptoms; // Initially show all symptoms
    });

    // Start the chat
    this.chatService.getStartMessage().subscribe((response) => {
      this.chatMessages.push({ sender: 'Assistant', message: response.message });
    });
  }

  // Open the dropdown
  openDropdown(): void {
    this.filteredSymptoms = this.availableSymptoms; // Reset filter
    this.dropdownOpen = true;
  }

  // Filter the symptoms based on the search term
  filterSymptoms(): void {
    this.filteredSymptoms = this.availableSymptoms.filter((symptom) =>
      symptom.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
    this.dropdownOpen = true; // Open dropdown when filtering
  }

  // Add a symptom from the search dropdown
  selectSymptomFromSearch(symptom: string): void {
    if (!this.selectedSymptoms.includes(symptom)) {
      this.selectedSymptoms.push(symptom);
    }
    this.searchTerm = ''; // Clear the search term
    this.dropdownOpen = false; // Close the dropdown
  }

  // Remove a symptom from the selected list
  removeSymptom(index: number): void {
    this.selectedSymptoms.splice(index, 1);
  }

  // Close the dropdown when clicking outside
  closeDropdown(): void {
    this.dropdownOpen = false;
  }

  // Handle chat messages
  sendMessage(): void {
    if (!this.userMessage.trim()) {
      return;
    }

    this.chatMessages.push({ sender: 'You', message: this.userMessage });

    this.chatService
      .sendUserResponse(this.userId, this.userMessage)
      .subscribe((response) => {
        this.chatMessages.push({ sender: 'Assistant', message: response.message });

        // If the assistant prompts for symptoms, mark chat as completed
        if (response.message.includes('select symptoms')) {
          this.chatCompleted = true;
        }
      });

    this.userMessage = ''; // Clear the input field
  }

  // Navigate back to the previous step
  goBackToAge(): void {
    this.router.navigate(['/virtual-assistant/patient']);
  }

  proceedToResults(): void {
    if (this.selectedSymptoms.length > 0) {
      this.symptomsService.predictDisease(this.userId, this.selectedSymptoms).subscribe(
        (response) => {
          console.log('Prediction Result:', response); // Debugging
          // Add symptoms to the response before saving
          const resultWithSymptoms = {
            ...response,
            selectedSymptoms: this.selectedSymptoms
          };
          this.resultsStateService.setPredictionResult(resultWithSymptoms); // Save result with symptoms
          this.router.navigate(['/virtual-assistant/results']); // Navigate without state
        },
        (error) => {
          console.error('Prediction Error:', error);
        }
      );
    }
  }

  // Detect clicks outside the dropdown
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.relative')) {
      this.closeDropdown();
    }
  }
}
