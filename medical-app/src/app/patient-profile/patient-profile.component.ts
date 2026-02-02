import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { ProfileService } from '../services/profile.service';
import { BookingService, BookingAppointment, TimeSlot } from '../services/booking.service';
import { HttpClient } from '@angular/common/http';
import {
  PatientProfile,
  UpdatePatientProfile,
  MedicalIdCard,
  Medication,
  BloodType,
  BLOOD_TYPE_DISPLAY
} from '../models/patient-profile.model';

@Component({
  selector: 'app-patient-profile',
  templateUrl: './patient-profile.component.html'
})
export class PatientProfileComponent implements OnInit {
  profile: PatientProfile | null = null;
  medicalIdCard: MedicalIdCard | null = null;
  isLoading = true;
  isSaving = false;
  activeTab = 'appointments';
  showMedicalIdCard = false;

  // Forms
  personalInfoForm!: FormGroup;
  medicalInfoForm!: FormGroup;
  emergencyContactForm!: FormGroup;

  // Arrays for editing
  allergies: string[] = [];
  chronicConditions: string[] = [];
  medications: Medication[] = [];

  // New item inputs
  newAllergy = '';
  newCondition = '';
  newMedication: Medication = { name: '', dosage: '', frequency: '' };

  // Blood type options
  bloodTypes: { value: BloodType; label: string }[] = [
    { value: 'A_POSITIVE', label: 'A+' },
    { value: 'A_NEGATIVE', label: 'A-' },
    { value: 'B_POSITIVE', label: 'B+' },
    { value: 'B_NEGATIVE', label: 'B-' },
    { value: 'AB_POSITIVE', label: 'AB+' },
    { value: 'AB_NEGATIVE', label: 'AB-' },
    { value: 'O_POSITIVE', label: 'O+' },
    { value: 'O_NEGATIVE', label: 'O-' },
    { value: 'UNKNOWN', label: 'Unknown' }
  ];

  // Alert
  showAlert = false;
  alertMsg = '';
  alertColor = 'green';

  // Avatar
  avatarPreview: string | null = null;
  isUploadingAvatar = false;

  // Appointments
  patientId: number | null = null;
  upcomingAppointments: BookingAppointment[] = [];
  pastAppointments: BookingAppointment[] = [];
  isLoadingAppointments = false;
  appointmentActiveTab: 'upcoming' | 'past' = 'upcoming';

  // Reschedule Modal
  showRescheduleModal = false;
  rescheduleAppointment: BookingAppointment | null = null;
  rescheduleDate: string = '';
  availableSlots: TimeSlot[] = [];
  selectedRescheduleSlot: TimeSlot | null = null;
  isLoadingSlots = false;
  isRescheduling = false;

  // Cancel Modal
  showCancelModal = false;
  cancelAppointment: BookingAppointment | null = null;
  cancelReason = '';
  isCancelling = false;

  constructor(
    private profileService: ProfileService,
    private bookingService: BookingService,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.initForms();
  }

  ngOnInit(): void {
    this.loadProfile();
    this.loadPatientId();
  }

  private loadPatientId(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.http.get<any[]>('http://localhost:8080/api/user/patients').subscribe(
        (patients) => {
          const patient = patients.find(p => p.email === user.email);
          if (patient) {
            this.patientId = patient.id;
            this.loadAppointments();
          }
        },
        (error) => console.error('Error loading patient info:', error)
      );
    }
  }

  private initForms(): void {
    this.personalInfoForm = this.fb.group({
      phoneNumber: ['', [Validators.pattern(/^[+]?[0-9\s-]{7,20}$/)]],
      dateOfBirth: [''],
      gender: [''],
      address: ['', [Validators.maxLength(500)]],
      city: ['', [Validators.maxLength(100)]],
      country: ['', [Validators.maxLength(100)]]
    });

    this.medicalInfoForm = this.fb.group({
      bloodType: [''],
      heightCm: ['', [Validators.min(0), Validators.max(300)]],
      weightKg: ['', [Validators.min(0), Validators.max(500)]]
    });

    this.emergencyContactForm = this.fb.group({
      emergencyContactName: ['', [Validators.maxLength(255)]],
      emergencyContactPhone: ['', [Validators.pattern(/^[+]?[0-9\s-]{7,20}$/)]],
      emergencyContactRelationship: ['', [Validators.maxLength(50)]]
    });
  }

  loadProfile(): void {
    this.isLoading = true;
    this.profileService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.populateForms(profile);
        this.allergies = [...(profile.allergies || [])];
        this.chronicConditions = [...(profile.chronicConditions || [])];
        this.medications = [...(profile.currentMedications || [])];
        this.avatarPreview = profile.avatarUrl || null;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load profile:', err);
        this.showAlertMessage('Failed to load profile. Please try again.', 'red');
        this.isLoading = false;
      }
    });
  }

  private populateForms(profile: PatientProfile): void {
    this.personalInfoForm.patchValue({
      phoneNumber: profile.phoneNumber || '',
      dateOfBirth: profile.dateOfBirth || '',
      gender: profile.gender || '',
      address: profile.address || '',
      city: profile.city || '',
      country: profile.country || ''
    });

    this.medicalInfoForm.patchValue({
      bloodType: profile.bloodType || '',
      heightCm: profile.heightCm || '',
      weightKg: profile.weightKg || ''
    });

    this.emergencyContactForm.patchValue({
      emergencyContactName: profile.emergencyContactName || '',
      emergencyContactPhone: profile.emergencyContactPhone || '',
      emergencyContactRelationship: profile.emergencyContactRelationship || ''
    });
  }

  saveProfile(): void {
    if (this.isSaving) return;

    this.isSaving = true;
    const updates: UpdatePatientProfile = {
      ...this.personalInfoForm.value,
      ...this.medicalInfoForm.value,
      ...this.emergencyContactForm.value,
      allergies: this.allergies,
      chronicConditions: this.chronicConditions,
      currentMedications: this.medications
    };

    // Clean up empty values
    Object.keys(updates).forEach(key => {
      const value = (updates as any)[key];
      if (value === '' || value === null) {
        delete (updates as any)[key];
      }
    });

    this.profileService.updateProfile(updates).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.showAlertMessage('Profile updated successfully!', 'green');
        this.isSaving = false;
      },
      error: (err) => {
        console.error('Failed to save profile:', err);
        this.showAlertMessage('Failed to save profile. Please try again.', 'red');
        this.isSaving = false;
      }
    });
  }

  // Allergy management
  addAllergy(): void {
    if (this.newAllergy.trim() && !this.allergies.includes(this.newAllergy.trim())) {
      this.allergies.push(this.newAllergy.trim());
      this.newAllergy = '';
    }
  }

  removeAllergy(index: number): void {
    this.allergies.splice(index, 1);
  }

  // Chronic condition management
  addCondition(): void {
    if (this.newCondition.trim() && !this.chronicConditions.includes(this.newCondition.trim())) {
      this.chronicConditions.push(this.newCondition.trim());
      this.newCondition = '';
    }
  }

  removeCondition(index: number): void {
    this.chronicConditions.splice(index, 1);
  }

  // Medication management
  addMedication(): void {
    if (this.newMedication.name.trim()) {
      this.medications.push({ ...this.newMedication });
      this.newMedication = { name: '', dosage: '', frequency: '' };
    }
  }

  removeMedication(index: number): void {
    this.medications.splice(index, 1);
  }

  // Avatar upload
  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.showAlertMessage('Please select an image file.', 'red');
        return;
      }

      // Validate file size (5MB max)
      if (file.size > 5 * 1024 * 1024) {
        this.showAlertMessage('Image size must be less than 5MB.', 'red');
        return;
      }

      this.isUploadingAvatar = true;
      this.profileService.uploadAvatar(file).subscribe({
        next: (response) => {
          this.avatarPreview = response.avatarUrl;
          if (this.profile) {
            this.profile.avatarUrl = response.avatarUrl;
          }
          this.showAlertMessage('Avatar uploaded successfully!', 'green');
          this.isUploadingAvatar = false;
        },
        error: (err) => {
          console.error('Failed to upload avatar:', err);
          this.showAlertMessage('Failed to upload avatar. Please try again.', 'red');
          this.isUploadingAvatar = false;
        }
      });
    }
  }

  deleteAvatar(): void {
    if (!this.avatarPreview) return;

    this.isUploadingAvatar = true;
    this.profileService.deleteAvatar().subscribe({
      next: () => {
        this.avatarPreview = null;
        if (this.profile) {
          this.profile.avatarUrl = undefined;
        }
        this.showAlertMessage('Avatar deleted successfully!', 'green');
        this.isUploadingAvatar = false;
      },
      error: (err) => {
        console.error('Failed to delete avatar:', err);
        this.showAlertMessage('Failed to delete avatar. Please try again.', 'red');
        this.isUploadingAvatar = false;
      }
    });
  }

  // Medical ID Card
  toggleMedicalIdCard(): void {
    if (!this.showMedicalIdCard && !this.medicalIdCard) {
      this.profileService.getMedicalIdCard().subscribe({
        next: (card) => {
          this.medicalIdCard = card;
          this.showMedicalIdCard = true;
        },
        error: (err) => {
          console.error('Failed to load medical ID card:', err);
          this.showAlertMessage('Failed to load medical ID card.', 'red');
        }
      });
    } else {
      this.showMedicalIdCard = !this.showMedicalIdCard;
    }
  }

  // ==================== APPOINTMENTS ====================

  loadAppointments(): void {
    if (!this.patientId) return;

    this.isLoadingAppointments = true;

    this.bookingService.getPatientUpcomingAppointments(this.patientId).subscribe({
      next: (appointments) => {
        this.upcomingAppointments = appointments;
        this.isLoadingAppointments = false;
      },
      error: (err) => {
        console.error('Failed to load upcoming appointments:', err);
        this.isLoadingAppointments = false;
      }
    });

    this.bookingService.getPatientPastAppointments(this.patientId).subscribe({
      next: (appointments) => {
        this.pastAppointments = appointments;
      },
      error: (err) => {
        console.error('Failed to load past appointments:', err);
      }
    });
  }

  setAppointmentTab(tab: 'upcoming' | 'past'): void {
    this.appointmentActiveTab = tab;
  }

  // Reschedule
  openRescheduleModal(appointment: BookingAppointment): void {
    this.rescheduleAppointment = appointment;
    this.rescheduleDate = '';
    this.availableSlots = [];
    this.selectedRescheduleSlot = null;
    this.showRescheduleModal = true;
  }

  closeRescheduleModal(): void {
    this.showRescheduleModal = false;
    this.rescheduleAppointment = null;
    this.rescheduleDate = '';
    this.availableSlots = [];
    this.selectedRescheduleSlot = null;
  }

  onRescheduleDateChange(): void {
    if (!this.rescheduleAppointment || !this.rescheduleDate) return;

    this.isLoadingSlots = true;
    this.availableSlots = [];
    this.selectedRescheduleSlot = null;

    this.bookingService.getAvailableSlots(this.rescheduleAppointment.doctorId, this.rescheduleDate).subscribe({
      next: (slots) => {
        this.availableSlots = slots.filter(s => s.isAvailable);
        this.isLoadingSlots = false;
      },
      error: (err) => {
        console.error('Failed to load slots:', err);
        this.isLoadingSlots = false;
      }
    });
  }

  selectRescheduleSlot(slot: TimeSlot): void {
    this.selectedRescheduleSlot = slot;
  }

  confirmReschedule(): void {
    if (!this.rescheduleAppointment || !this.selectedRescheduleSlot) return;

    this.isRescheduling = true;

    this.bookingService.rescheduleAppointment(
      this.rescheduleAppointment.id!,
      this.selectedRescheduleSlot.dateTime
    ).subscribe({
      next: () => {
        this.showAlertMessage('Appointment rescheduled successfully!', 'green');
        this.closeRescheduleModal();
        this.loadAppointments();
        this.isRescheduling = false;
      },
      error: (err) => {
        console.error('Failed to reschedule:', err);
        this.showAlertMessage('Failed to reschedule appointment. Please try again.', 'red');
        this.isRescheduling = false;
      }
    });
  }

  // Cancel
  openCancelModal(appointment: BookingAppointment): void {
    this.cancelAppointment = appointment;
    this.cancelReason = '';
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelAppointment = null;
    this.cancelReason = '';
  }

  confirmCancel(): void {
    if (!this.cancelAppointment) return;

    this.isCancelling = true;

    this.bookingService.cancelAppointment(
      this.cancelAppointment.id!,
      this.cancelReason || undefined
    ).subscribe({
      next: () => {
        this.showAlertMessage('Appointment cancelled successfully.', 'green');
        this.closeCancelModal();
        this.loadAppointments();
        this.isCancelling = false;
      },
      error: (err) => {
        console.error('Failed to cancel:', err);
        this.showAlertMessage('Failed to cancel appointment. Please try again.', 'red');
        this.isCancelling = false;
      }
    });
  }

  // Helpers
  formatAppointmentDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  formatAppointmentTime(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getStatusColor(status: string | undefined): string {
    switch (status?.toUpperCase()) {
      case 'SCHEDULED': return 'bg-blue-500/20 text-blue-400';
      case 'CONFIRMED': return 'bg-green-500/20 text-green-400';
      case 'COMPLETED': return 'bg-gray-500/20 text-gray-400';
      case 'CANCELLED': return 'bg-red-500/20 text-red-400';
      default: return 'bg-gray-500/20 text-gray-400';
    }
  }

  getMinDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Tab navigation
  setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'appointments' && this.patientId && this.upcomingAppointments.length === 0) {
      this.loadAppointments();
    }
  }

  // Alert helper
  private showAlertMessage(msg: string, color: string): void {
    this.alertMsg = msg;
    this.alertColor = color;
    this.showAlert = true;

    setTimeout(() => {
      this.showAlert = false;
    }, 5000);
  }

  // Get blood type display name
  getBloodTypeDisplay(type: BloodType | undefined): string {
    return type ? BLOOD_TYPE_DISPLAY[type] : 'Not set';
  }

  // Calculate BMI category
  getBmiCategory(bmi: number | undefined): string {
    if (!bmi) return '';
    if (bmi < 18.5) return 'Underweight';
    if (bmi < 25) return 'Normal';
    if (bmi < 30) return 'Overweight';
    return 'Obese';
  }

  getBmiColor(bmi: number | undefined): string {
    if (!bmi) return 'text-gray-400';
    if (bmi < 18.5) return 'text-yellow-400';
    if (bmi < 25) return 'text-green-400';
    if (bmi < 30) return 'text-yellow-400';
    return 'text-red-400';
  }

  // ==================== GOOGLE CALENDAR ====================

  addToGoogleCalendar(appointment: BookingAppointment): void {
    const title = encodeURIComponent(`Medical Appointment - ${appointment.doctorName}`);
    const startDate = this.formatDateTimeForGoogle(appointment.appointmentDate);
    const endDate = this.formatDateTimeForGoogle(this.addMinutesToDateTime(appointment.appointmentDate, 30));

    const details = encodeURIComponent(
      `Appointment with ${appointment.doctorName}\n` +
      `Specialization: ${appointment.doctorSpecialization}\n` +
      `Reason: ${appointment.reason || 'General consultation'}\n\n` +
      `Booked via Medical You`
    );

    const location = encodeURIComponent('Medical You Clinic');

    const googleCalendarUrl =
      `https://calendar.google.com/calendar/render?action=TEMPLATE` +
      `&text=${title}` +
      `&dates=${startDate}/${endDate}` +
      `&details=${details}` +
      `&location=${location}`;

    window.open(googleCalendarUrl, '_blank');
  }

  downloadICalFile(appointment: BookingAppointment): void {
    const startDate = new Date(appointment.appointmentDate);
    const endDate = new Date(startDate.getTime() + 30 * 60 * 1000);

    const icsContent = this.generateICSContent(
      `Medical Appointment - ${appointment.doctorName}`,
      `Appointment with ${appointment.doctorName}\nSpecialization: ${appointment.doctorSpecialization}\nReason: ${appointment.reason || 'General consultation'}`,
      'Medical You Clinic',
      startDate,
      endDate
    );

    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `appointment-${appointment.doctorName?.replace(/\s+/g, '-').toLowerCase()}.ics`;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  private formatDateTimeForGoogle(dateTimeStr: string): string {
    const date = new Date(dateTimeStr);
    return date.toISOString().replace(/-|:|\.\d{3}/g, '').slice(0, 15) + 'Z';
  }

  private addMinutesToDateTime(dateTimeStr: string, minutes: number): string {
    const date = new Date(dateTimeStr);
    date.setMinutes(date.getMinutes() + minutes);
    return date.toISOString();
  }

  private generateICSContent(title: string, description: string, location: string, start: Date, end: Date): string {
    const formatICSDate = (date: Date) => {
      return date.toISOString().replace(/-|:|\.\d{3}/g, '').slice(0, 15) + 'Z';
    };

    return `BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//Medical You//Appointment Booking//EN
BEGIN:VEVENT
UID:${Date.now()}@medicalyou.com
DTSTAMP:${formatICSDate(new Date())}
DTSTART:${formatICSDate(start)}
DTEND:${formatICSDate(end)}
SUMMARY:${title}
DESCRIPTION:${description.replace(/\n/g, '\\n')}
LOCATION:${location}
STATUS:CONFIRMED
BEGIN:VALARM
ACTION:DISPLAY
DESCRIPTION:Reminder
TRIGGER:-PT1H
END:VALARM
BEGIN:VALARM
ACTION:DISPLAY
DESCRIPTION:Reminder
TRIGGER:-PT24H
END:VALARM
END:VEVENT
END:VCALENDAR`;
  }
}
