import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PatientProfile,
  UpdatePatientProfile,
  MedicalIdCard
} from '../models/patient-profile.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = 'http://localhost:8080/api/patient/profile';

  constructor(private http: HttpClient) {}

  /**
   * Get current user's profile
   */
  getMyProfile(): Observable<PatientProfile> {
    return this.http.get<PatientProfile>(this.apiUrl);
  }

  /**
   * Get profile by patient ID
   */
  getProfileById(patientId: number): Observable<PatientProfile> {
    return this.http.get<PatientProfile>(`${this.apiUrl}/${patientId}`);
  }

  /**
   * Get profile by medical ID (for emergency access)
   */
  getProfileByMedicalId(medicalId: string): Observable<PatientProfile> {
    return this.http.get<PatientProfile>(`${this.apiUrl}/medical-id/${medicalId}`);
  }

  /**
   * Update current user's profile
   */
  updateProfile(updates: UpdatePatientProfile): Observable<PatientProfile> {
    return this.http.put<PatientProfile>(this.apiUrl, updates);
  }

  /**
   * Upload avatar
   */
  uploadAvatar(file: File): Observable<{ status: string; message: string; avatarUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ status: string; message: string; avatarUrl: string }>(
      `${this.apiUrl}/avatar`,
      formData
    );
  }

  /**
   * Delete avatar
   */
  deleteAvatar(): Observable<{ status: string; message: string }> {
    return this.http.delete<{ status: string; message: string }>(`${this.apiUrl}/avatar`);
  }

  /**
   * Get Medical ID Card data
   */
  getMedicalIdCard(): Observable<MedicalIdCard> {
    return this.http.get<MedicalIdCard>(`${this.apiUrl}/medical-id-card`);
  }

  /**
   * Get Medical ID Card by patient ID
   */
  getMedicalIdCardByPatientId(patientId: number): Observable<MedicalIdCard> {
    return this.http.get<MedicalIdCard>(`${this.apiUrl}/${patientId}/medical-id-card`);
  }
}
