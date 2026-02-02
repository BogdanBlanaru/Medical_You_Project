import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TimeSlot {
  date: string;
  startTime: string;
  endTime: string;
  dateTime: string;
  isAvailable: boolean;
  displayTime: string;
}

export interface DoctorAvailability {
  id: number;
  doctorId: number;
  dayOfWeek: number;
  dayName: string;
  startTime: string;
  endTime: string;
  slotDurationMinutes: number;
  isActive: boolean;
}

export interface BookingAppointment {
  id?: number;
  doctorId: number;
  doctorName?: string;
  doctorSpecialization?: string;
  patientId: number;
  patientName?: string;
  appointmentDate: string;
  status?: string;
  reason?: string;
  notes?: string;
  createdAt?: string;
  isCancelled?: boolean;
}

export interface BookAppointmentRequest {
  patientId: number;
  doctorId: number;
  dateTime: string;
  reason: string;
}

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private apiUrl = 'http://localhost:8080/api/booking';

  constructor(private http: HttpClient) {}

  // ==================== AVAILABILITY ====================

  /**
   * Get doctor's weekly schedule
   */
  getDoctorSchedule(doctorId: number): Observable<DoctorAvailability[]> {
    return this.http.get<DoctorAvailability[]>(`${this.apiUrl}/doctor/${doctorId}/schedule`);
  }

  // ==================== TIME SLOTS ====================

  /**
   * Get available time slots for a doctor on a specific date
   */
  getAvailableSlots(doctorId: number, date: string): Observable<TimeSlot[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<TimeSlot[]>(`${this.apiUrl}/doctor/${doctorId}/slots`, { params });
  }

  /**
   * Get available slots for a date range
   */
  getAvailableSlotsForRange(
    doctorId: number,
    startDate: string,
    endDate: string
  ): Observable<{ [key: string]: TimeSlot[] }> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<{ [key: string]: TimeSlot[] }>(
      `${this.apiUrl}/doctor/${doctorId}/slots/range`,
      { params }
    );
  }

  /**
   * Check if a specific slot is available
   */
  checkSlotAvailability(doctorId: number, dateTime: string): Observable<{ available: boolean }> {
    const params = new HttpParams().set('dateTime', dateTime);
    return this.http.get<{ available: boolean }>(
      `${this.apiUrl}/doctor/${doctorId}/slot-available`,
      { params }
    );
  }

  // ==================== BOOKING ====================

  /**
   * Book an appointment
   */
  bookAppointment(request: BookAppointmentRequest): Observable<BookingAppointment> {
    return this.http.post<BookingAppointment>(`${this.apiUrl}/book`, request);
  }

  /**
   * Reschedule an appointment
   */
  rescheduleAppointment(appointmentId: number, newDateTime: string): Observable<BookingAppointment> {
    return this.http.put<BookingAppointment>(
      `${this.apiUrl}/appointment/${appointmentId}/reschedule`,
      { newDateTime }
    );
  }

  /**
   * Cancel an appointment
   */
  cancelAppointment(appointmentId: number, reason?: string): Observable<{ status: string; message: string }> {
    const params = reason ? new HttpParams().set('reason', reason) : undefined;
    return this.http.delete<{ status: string; message: string }>(
      `${this.apiUrl}/appointment/${appointmentId}/cancel`,
      { params }
    );
  }

  /**
   * Confirm an appointment (doctor action)
   */
  confirmAppointment(appointmentId: number): Observable<BookingAppointment> {
    return this.http.put<BookingAppointment>(
      `${this.apiUrl}/appointment/${appointmentId}/confirm`,
      {}
    );
  }

  // ==================== PATIENT APPOINTMENTS ====================

  /**
   * Get patient's upcoming appointments
   */
  getPatientUpcomingAppointments(patientId: number): Observable<BookingAppointment[]> {
    return this.http.get<BookingAppointment[]>(`${this.apiUrl}/patient/${patientId}/upcoming`);
  }

  /**
   * Get patient's past appointments
   */
  getPatientPastAppointments(patientId: number): Observable<BookingAppointment[]> {
    return this.http.get<BookingAppointment[]>(`${this.apiUrl}/patient/${patientId}/history`);
  }
}
