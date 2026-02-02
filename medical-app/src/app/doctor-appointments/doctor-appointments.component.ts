import { Component, OnInit } from '@angular/core';
import { Doctor } from '../models/doctor.model';
import { DoctorService } from '../services/doctor.service';
import { BookingService, TimeSlot, BookAppointmentRequest, DoctorAvailability } from '../services/booking.service';
import { HttpClient } from '@angular/common/http';

interface CalendarDay {
  date: Date;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  hasSlots: boolean;
  isPast: boolean;
}

@Component({
  selector: 'app-doctor-appointments',
  templateUrl: './doctor-appointments.component.html',
  styleUrls: ['./doctor-appointments.component.css'],
})
export class DoctorAppointmentsComponent implements OnInit {
  // Step tracking
  currentStep: number = 1;
  steps = [
    { number: 1, title: 'Select Doctor', description: 'Choose your healthcare provider' },
    { number: 2, title: 'Pick Date & Time', description: 'Select available slot' },
    { number: 3, title: 'Confirm Booking', description: 'Review and confirm' }
  ];

  // Step 1: Doctor selection
  doctors: Doctor[] = [];
  filteredDoctors: Doctor[] = [];
  specializations: (string | undefined)[] = [];
  selectedSpecialization: string = 'All';
  isLoading: boolean = true;
  errorMessage: string = '';
  selectedDoctor: Doctor | null = null;

  // Step 2: Date & Time selection
  currentMonth: Date = new Date();
  calendarDays: CalendarDay[] = [];
  weekDays: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  selectedDate: Date | null = null;
  availableSlots: TimeSlot[] = [];
  selectedSlot: TimeSlot | null = null;
  isLoadingSlots: boolean = false;
  doctorSchedule: DoctorAvailability[] = [];

  // Step 3: Confirmation
  appointmentReason: string = '';
  isSubmitting: boolean = false;
  bookingSuccess: boolean = false;
  bookingError: string = '';

  // Patient info
  patientId: number | null = null;
  patientName: string = '';

  constructor(
    private doctorService: DoctorService,
    private bookingService: BookingService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadDoctors();
    this.loadPatientInfo();
    this.generateCalendar();
  }

  // ==================== PATIENT INFO ====================

  loadPatientInfo(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      // Fetch patient details by email
      this.http.get<any[]>('http://localhost:8080/api/user/patients').subscribe(
        (patients) => {
          const patient = patients.find(p => p.email === user.email);
          if (patient) {
            this.patientId = patient.id;
            this.patientName = patient.name;
          }
        },
        (error) => console.error('Error loading patient info:', error)
      );
    }
  }

  // ==================== STEP 1: DOCTOR SELECTION ====================

  loadDoctors(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorService.getAllDoctors().subscribe(
      (doctors) => {
        this.doctors = doctors;
        this.filteredDoctors = doctors;
        this.specializations = [
          ...new Set(doctors.map((d) => d.specialization).filter(s => s !== undefined)),
        ];
        this.isLoading = false;
      },
      (error) => {
        console.error('Error loading doctors:', error);
        this.errorMessage = 'Failed to load doctors. Please try again later.';
        this.isLoading = false;
      }
    );
  }

  onSpecializationChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedSpecialization = selectElement.value;

    if (this.selectedSpecialization === 'All') {
      this.filteredDoctors = this.doctors;
    } else {
      this.filteredDoctors = this.doctors.filter(
        (doctor) => doctor.specialization === this.selectedSpecialization
      );
    }
  }

  selectDoctor(doctor: Doctor): void {
    this.selectedDoctor = doctor;
    this.loadDoctorSchedule(doctor.id!);
    this.goToStep(2);
  }

  // ==================== STEP 2: DATE & TIME SELECTION ====================

  loadDoctorSchedule(doctorId: number): void {
    this.bookingService.getDoctorSchedule(doctorId).subscribe(
      (schedule) => {
        this.doctorSchedule = schedule;
        this.generateCalendar();
      },
      (error) => console.error('Error loading doctor schedule:', error)
    );
  }

  generateCalendar(): void {
    this.calendarDays = [];
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Add days from previous month
    const startDayOfWeek = firstDay.getDay();
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const date = new Date(year, month, -i);
      this.calendarDays.push(this.createCalendarDay(date, false, today));
    }

    // Add days of current month
    for (let day = 1; day <= lastDay.getDate(); day++) {
      const date = new Date(year, month, day);
      this.calendarDays.push(this.createCalendarDay(date, true, today));
    }

    // Add days from next month
    const remainingDays = 42 - this.calendarDays.length;
    for (let i = 1; i <= remainingDays; i++) {
      const date = new Date(year, month + 1, i);
      this.calendarDays.push(this.createCalendarDay(date, false, today));
    }
  }

  createCalendarDay(date: Date, isCurrentMonth: boolean, today: Date): CalendarDay {
    const isPast = date < today;
    const hasSlots = this.checkIfDayHasSlots(date);

    return {
      date: date,
      dayNumber: date.getDate(),
      isCurrentMonth: isCurrentMonth,
      isToday: date.getTime() === today.getTime(),
      isSelected: this.selectedDate ? date.getTime() === this.selectedDate.getTime() : false,
      hasSlots: hasSlots && !isPast,
      isPast: isPast
    };
  }

  checkIfDayHasSlots(date: Date): boolean {
    if (!this.doctorSchedule || this.doctorSchedule.length === 0) return true;
    const dayOfWeek = date.getDay();
    return this.doctorSchedule.some(s => s.dayOfWeek === dayOfWeek && s.isActive);
  }

  previousMonth(): void {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() - 1,
      1
    );
    this.generateCalendar();
  }

  nextMonth(): void {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() + 1,
      1
    );
    this.generateCalendar();
  }

  selectDate(day: CalendarDay): void {
    if (day.isPast || !day.hasSlots) return;

    this.selectedDate = day.date;
    this.selectedSlot = null;
    this.generateCalendar();
    this.loadAvailableSlots();
  }

  loadAvailableSlots(): void {
    if (!this.selectedDoctor || !this.selectedDate) return;

    this.isLoadingSlots = true;
    this.availableSlots = [];

    const dateStr = this.formatDate(this.selectedDate);

    this.bookingService.getAvailableSlots(this.selectedDoctor.id!, dateStr).subscribe(
      (slots) => {
        this.availableSlots = slots.filter(s => s.isAvailable);
        this.isLoadingSlots = false;
      },
      (error) => {
        console.error('Error loading slots:', error);
        this.isLoadingSlots = false;
      }
    );
  }

  selectSlot(slot: TimeSlot): void {
    if (!slot.isAvailable) return;
    this.selectedSlot = slot;
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // ==================== STEP 3: CONFIRMATION ====================

  confirmBooking(): void {
    if (!this.selectedDoctor || !this.selectedSlot || !this.patientId) {
      this.bookingError = 'Missing required information. Please try again.';
      return;
    }

    this.isSubmitting = true;
    this.bookingError = '';

    const request: BookAppointmentRequest = {
      patientId: this.patientId,
      doctorId: this.selectedDoctor.id!,
      dateTime: this.selectedSlot.dateTime,
      reason: this.appointmentReason || 'General consultation'
    };

    this.bookingService.bookAppointment(request).subscribe(
      (response) => {
        this.bookingSuccess = true;
        this.isSubmitting = false;
      },
      (error) => {
        console.error('Error booking appointment:', error);
        this.bookingError = error.error?.message || 'Failed to book appointment. Please try again.';
        this.isSubmitting = false;
      }
    );
  }

  // ==================== NAVIGATION ====================

  goToStep(step: number): void {
    if (step === 2 && !this.selectedDoctor) return;
    if (step === 3 && (!this.selectedDoctor || !this.selectedSlot)) return;
    this.currentStep = step;
  }

  canProceedToStep2(): boolean {
    return !!this.selectedDoctor;
  }

  canProceedToStep3(): boolean {
    return !!this.selectedDoctor && !!this.selectedSlot;
  }

  resetWizard(): void {
    this.currentStep = 1;
    this.selectedDoctor = null;
    this.selectedDate = null;
    this.selectedSlot = null;
    this.appointmentReason = '';
    this.bookingSuccess = false;
    this.bookingError = '';
    this.availableSlots = [];
  }

  // ==================== HELPERS ====================

  getMonthName(): string {
    return this.currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }

  formatSelectedDate(): string {
    if (!this.selectedDate) return '';
    return this.selectedDate.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  // ==================== GOOGLE CALENDAR ====================

  addToGoogleCalendar(): void {
    if (!this.selectedDoctor || !this.selectedSlot) return;

    const title = encodeURIComponent(`Medical Appointment - Dr. ${this.selectedDoctor.name}`);
    const startDate = this.formatDateTimeForGoogle(this.selectedSlot.dateTime);
    const endDate = this.formatDateTimeForGoogle(this.addMinutesToDateTime(this.selectedSlot.dateTime, 30));

    const details = encodeURIComponent(
      `Appointment with Dr. ${this.selectedDoctor.name}\n` +
      `Specialization: ${this.selectedDoctor.specialization}\n` +
      `Reason: ${this.appointmentReason || 'General consultation'}\n\n` +
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

  downloadICalFile(): void {
    if (!this.selectedDoctor || !this.selectedSlot) return;

    const startDate = new Date(this.selectedSlot.dateTime);
    const endDate = new Date(startDate.getTime() + 30 * 60 * 1000); // 30 minutes

    const icsContent = this.generateICSContent(
      `Medical Appointment - Dr. ${this.selectedDoctor.name}`,
      `Appointment with Dr. ${this.selectedDoctor.name}\nSpecialization: ${this.selectedDoctor.specialization}\nReason: ${this.appointmentReason || 'General consultation'}`,
      'Medical You Clinic',
      startDate,
      endDate
    );

    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `appointment-dr-${this.selectedDoctor.name.replace(/\s+/g, '-').toLowerCase()}.ics`;
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
