import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AppointmentDto {
  id?: number;
  doctorId?: number;
  doctorName?: string;
  doctorSpecialization?: string;
  patientId?: number;
  patientName?: string;
  appointmentDate: string;
  status?: string;
  reason?: string;
  notes?: string;
  createdAt?: string;
  isCancelled?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  private baseUrl = 'http://localhost:8080/api/appointments';

  constructor(private http: HttpClient) {}

  /**
   * Create a new appointment
   */
  createAppointment(appointment: AppointmentDto): Observable<AppointmentDto> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post<AppointmentDto>(`${this.baseUrl}/create`, appointment, { headers });
  }

  /**
   * Get appointment by ID
   */
  getAppointmentById(id: number): Observable<AppointmentDto> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<AppointmentDto>(`${this.baseUrl}/${id}`, { headers });
  }

  /**
   * Get all appointments for a specific doctor
   */
  getAppointmentsByDoctor(doctorId: number): Observable<AppointmentDto[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<AppointmentDto[]>(`${this.baseUrl}/doctor/${doctorId}`, { headers });
  }

  /**
   * Get all appointments for a specific patient
   */
  getAppointmentsByPatient(patientId: number): Observable<AppointmentDto[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<AppointmentDto[]>(`${this.baseUrl}/patient/${patientId}`, { headers });
  }

  /**
   * Update an existing appointment
   */
  updateAppointment(id: number, appointment: AppointmentDto): Observable<AppointmentDto> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.put<AppointmentDto>(`${this.baseUrl}/${id}`, appointment, { headers });
  }

  /**
   * Cancel an appointment
   */
  cancelAppointment(id: number): Observable<string> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.delete<string>(`${this.baseUrl}/${id}/cancel`, { headers, responseType: 'text' as 'json' });
  }

  /**
   * Get all appointments (admin only)
   */
  getAllAppointments(): Observable<AppointmentDto[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<AppointmentDto[]>(`${this.baseUrl}/all`, { headers });
  }
}
