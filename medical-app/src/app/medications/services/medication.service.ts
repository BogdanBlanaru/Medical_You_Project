import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Medication,
  CreateMedication,
  MedicationDashboard,
  MedicationLog,
  PagedResponse
} from '../models/medication.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MedicationService {
  private readonly apiUrl = `${environment.apiUrl}/medications`;

  constructor(private http: HttpClient) {}

  // CRUD
  createMedication(medication: CreateMedication): Observable<Medication> {
    return this.http.post<Medication>(this.apiUrl, medication);
  }

  getMedication(id: number): Observable<Medication> {
    return this.http.get<Medication>(`${this.apiUrl}/${id}`);
  }

  updateMedication(id: number, medication: CreateMedication): Observable<Medication> {
    return this.http.put<Medication>(`${this.apiUrl}/${id}`, medication);
  }

  deleteMedication(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // List
  getMedications(
    familyMemberId?: number,
    activeOnly: boolean = false,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<Medication>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('activeOnly', activeOnly.toString());

    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }

    return this.http.get<PagedResponse<Medication>>(this.apiUrl, { params });
  }

  getTodaySchedule(familyMemberId?: number): Observable<Medication[]> {
    let params = new HttpParams();
    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    return this.http.get<Medication[]>(`${this.apiUrl}/today`, { params });
  }

  getMedicationsNeedingRefill(): Observable<Medication[]> {
    return this.http.get<Medication[]>(`${this.apiUrl}/refill`);
  }

  // Logging
  takeMedication(id: number, notes?: string): Observable<MedicationLog> {
    let params = new HttpParams();
    if (notes) {
      params = params.set('notes', notes);
    }
    return this.http.post<MedicationLog>(`${this.apiUrl}/${id}/take`, {}, { params });
  }

  skipMedication(id: number, notes?: string): Observable<MedicationLog> {
    let params = new HttpParams();
    if (notes) {
      params = params.set('notes', notes);
    }
    return this.http.post<MedicationLog>(`${this.apiUrl}/${id}/skip`, {}, { params });
  }

  getMedicationLogs(id: number, days: number = 30): Observable<MedicationLog[]> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<MedicationLog[]>(`${this.apiUrl}/${id}/logs`, { params });
  }

  // Status
  pauseMedication(id: number): Observable<Medication> {
    return this.http.post<Medication>(`${this.apiUrl}/${id}/pause`, {});
  }

  resumeMedication(id: number): Observable<Medication> {
    return this.http.post<Medication>(`${this.apiUrl}/${id}/resume`, {});
  }

  completeMedication(id: number): Observable<Medication> {
    return this.http.post<Medication>(`${this.apiUrl}/${id}/complete`, {});
  }

  // Dashboard
  getDashboard(familyMemberId?: number): Observable<MedicationDashboard> {
    let params = new HttpParams();
    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    return this.http.get<MedicationDashboard>(`${this.apiUrl}/dashboard`, { params });
  }

  getAdherenceRate(id: number, days: number = 30): Observable<number> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<number>(`${this.apiUrl}/${id}/adherence`, { params });
  }
}
