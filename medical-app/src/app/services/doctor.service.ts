import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor } from '../models/doctor.model';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private baseUrl = 'http://localhost:8080/api/user/doctors';

  constructor(private http: HttpClient) {}

  /**
   * Get all doctors from the backend
   */
  getAllDoctors(): Observable<Doctor[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<Doctor[]>(this.baseUrl, { headers });
  }

  /**
   * Get doctors filtered by specialization
   */
  getDoctorsBySpecialization(specialization: string): Observable<Doctor[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<Doctor[]>(`${this.baseUrl}/specialization/${specialization}`, { headers });
  }
}
