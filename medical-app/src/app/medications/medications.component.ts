import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MedicationService } from './services/medication.service';
import {
  Medication,
  MedicationDashboard,
  CreateMedication,
  MedicationFrequency,
  MedicationStatus,
  MedicationLogStatus,
  getFrequencyLabel,
  getStatusLabel,
  getLogStatusLabel,
  MEDICATION_COLORS
} from './models/medication.model';

@Component({
  selector: 'app-medications',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './medications.component.html'
})
export class MedicationsComponent implements OnInit {
  dashboard: MedicationDashboard | null = null;
  medications: Medication[] = [];
  selectedMedication: Medication | null = null;
  isLoading = true;
  showAddModal = false;
  showTakeModal = false;
  activeTab: 'today' | 'all' | 'refill' = 'today';

  medicationForm!: FormGroup;
  frequencies = Object.values(MedicationFrequency);
  colors = MEDICATION_COLORS;
  takeNotes = '';

  // Alert
  showAlert = false;
  alertMsg = '';
  alertColor = 'green';

  constructor(
    private medicationService: MedicationService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDashboard();
  }

  initForm(): void {
    this.medicationForm = this.fb.group({
      name: ['', Validators.required],
      dosage: [''],
      frequency: [MedicationFrequency.ONCE_DAILY, Validators.required],
      timesPerDay: [1],
      specificTimes: [''],
      instructions: [''],
      prescribedBy: [''],
      startDate: [new Date().toISOString().split('T')[0], Validators.required],
      endDate: [''],
      refillReminderDays: [7],
      pillsRemaining: [''],
      pillsPerDose: [1],
      color: ['#3B82F6'],
      notes: ['']
    });
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.medicationService.getDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard = dashboard;
        this.medications = dashboard.todaySchedule;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.isLoading = false;
      }
    });
  }

  loadAllMedications(): void {
    this.medicationService.getMedications(undefined, false).subscribe({
      next: (response) => {
        this.medications = response.content;
      },
      error: (err) => console.error('Error loading medications:', err)
    });
  }

  changeTab(tab: 'today' | 'all' | 'refill'): void {
    this.activeTab = tab;
    switch (tab) {
      case 'today':
        this.medications = this.dashboard?.todaySchedule || [];
        break;
      case 'all':
        this.loadAllMedications();
        break;
      case 'refill':
        this.medications = this.dashboard?.needRefill || [];
        break;
    }
  }

  // ==================== Add/Edit Modal ====================

  openAddModal(): void {
    this.selectedMedication = null;
    this.medicationForm.reset({
      frequency: MedicationFrequency.ONCE_DAILY,
      timesPerDay: 1,
      startDate: new Date().toISOString().split('T')[0],
      refillReminderDays: 7,
      pillsPerDose: 1,
      color: '#3B82F6'
    });
    this.showAddModal = true;
  }

  openEditModal(medication: Medication): void {
    this.selectedMedication = medication;
    this.medicationForm.patchValue({
      name: medication.name,
      dosage: medication.dosage,
      frequency: medication.frequency,
      timesPerDay: medication.timesPerDay,
      specificTimes: medication.specificTimes?.join(', ') || '',
      instructions: medication.instructions,
      prescribedBy: medication.prescribedBy,
      startDate: medication.startDate,
      endDate: medication.endDate,
      refillReminderDays: medication.refillReminderDays,
      pillsRemaining: medication.pillsRemaining,
      pillsPerDose: medication.pillsPerDose,
      color: medication.color || '#3B82F6',
      notes: medication.notes
    });
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.selectedMedication = null;
  }

  saveMedication(): void {
    if (this.medicationForm.invalid) return;

    const formValue = this.medicationForm.value;
    const specificTimes = formValue.specificTimes
      ? formValue.specificTimes.split(',').map((t: string) => t.trim()).filter((t: string) => t)
      : [];

    const medication: CreateMedication = {
      name: formValue.name,
      dosage: formValue.dosage || undefined,
      frequency: formValue.frequency,
      timesPerDay: formValue.timesPerDay,
      specificTimes: specificTimes.length > 0 ? specificTimes : undefined,
      instructions: formValue.instructions || undefined,
      prescribedBy: formValue.prescribedBy || undefined,
      startDate: formValue.startDate,
      endDate: formValue.endDate || undefined,
      refillReminderDays: formValue.refillReminderDays || undefined,
      pillsRemaining: formValue.pillsRemaining || undefined,
      pillsPerDose: formValue.pillsPerDose || undefined,
      color: formValue.color || undefined,
      notes: formValue.notes || undefined
    };

    const request = this.selectedMedication
      ? this.medicationService.updateMedication(this.selectedMedication.id, medication)
      : this.medicationService.createMedication(medication);

    request.subscribe({
      next: () => {
        this.showAlertMessage(
          this.selectedMedication ? 'Medicament actualizat!' : 'Medicament adăugat!',
          'green'
        );
        this.closeAddModal();
        this.loadDashboard();
      },
      error: (err) => {
        console.error('Error saving medication:', err);
        this.showAlertMessage('Eroare la salvare', 'red');
      }
    });
  }

  // ==================== Take/Skip Modal ====================

  openTakeModal(medication: Medication): void {
    this.selectedMedication = medication;
    this.takeNotes = '';
    this.showTakeModal = true;
  }

  closeTakeModal(): void {
    this.showTakeModal = false;
    this.selectedMedication = null;
    this.takeNotes = '';
  }

  takeMedication(): void {
    if (!this.selectedMedication) return;

    this.medicationService.takeMedication(this.selectedMedication.id, this.takeNotes || undefined).subscribe({
      next: () => {
        this.showAlertMessage('✅ Medicament marcat ca luat!', 'green');
        this.closeTakeModal();
        this.loadDashboard();
      },
      error: (err) => {
        console.error('Error taking medication:', err);
        this.showAlertMessage('Eroare', 'red');
      }
    });
  }

  skipMedication(): void {
    if (!this.selectedMedication) return;

    this.medicationService.skipMedication(this.selectedMedication.id, this.takeNotes || undefined).subscribe({
      next: () => {
        this.showAlertMessage('⏭️ Medicament sărit', 'yellow');
        this.closeTakeModal();
        this.loadDashboard();
      },
      error: (err) => {
        console.error('Error skipping medication:', err);
        this.showAlertMessage('Eroare', 'red');
      }
    });
  }

  // ==================== Status Actions ====================

  pauseMedication(medication: Medication): void {
    this.medicationService.pauseMedication(medication.id).subscribe({
      next: () => {
        this.showAlertMessage('Medicament pus pe pauză', 'yellow');
        this.loadDashboard();
      },
      error: (err) => console.error('Error pausing medication:', err)
    });
  }

  resumeMedication(medication: Medication): void {
    this.medicationService.resumeMedication(medication.id).subscribe({
      next: () => {
        this.showAlertMessage('Medicament reluat', 'green');
        this.loadDashboard();
      },
      error: (err) => console.error('Error resuming medication:', err)
    });
  }

  completeMedication(medication: Medication): void {
    if (!confirm('Marchezi acest tratament ca finalizat?')) return;

    this.medicationService.completeMedication(medication.id).subscribe({
      next: () => {
        this.showAlertMessage('Tratament finalizat', 'green');
        this.loadDashboard();
      },
      error: (err) => console.error('Error completing medication:', err)
    });
  }

  deleteMedication(medication: Medication): void {
    if (!confirm('Sigur vrei să ștergi acest medicament?')) return;

    this.medicationService.deleteMedication(medication.id).subscribe({
      next: () => {
        this.showAlertMessage('Medicament șters', 'green');
        this.loadDashboard();
      },
      error: (err) => console.error('Error deleting medication:', err)
    });
  }

  // ==================== Helpers ====================

  getFrequencyLabel = getFrequencyLabel;
  getStatusLabel = getStatusLabel;
  getLogStatusLabel = getLogStatusLabel;

  getStatusClass(status: MedicationStatus): string {
    switch (status) {
      case MedicationStatus.ACTIVE: return 'bg-green-100 text-green-800';
      case MedicationStatus.PAUSED: return 'bg-yellow-100 text-yellow-800';
      case MedicationStatus.COMPLETED: return 'bg-blue-100 text-blue-800';
      case MedicationStatus.DISCONTINUED: return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getAdherenceClass(rate: number): string {
    if (rate >= 90) return 'text-green-500';
    if (rate >= 70) return 'text-yellow-500';
    return 'text-red-500';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ro-RO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  formatTime(timeStr: string): string {
    return new Date(timeStr).toLocaleTimeString('ro-RO', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  showAlertMessage(msg: string, color: string): void {
    this.alertMsg = msg;
    this.alertColor = color;
    this.showAlert = true;
    setTimeout(() => {
      this.showAlert = false;
    }, 4000);
  }

  protected readonly MedicationStatus = MedicationStatus;
}
