import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  HealthReading,
  CreateHealthReading,
  HealthStats,
  DashboardSummary,
  ChartDataPoint,
  HealthAlert,
  ReadingType,
  PagedResponse,
  ReadingTypeInfo
} from '../models/health-reading.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HealthTrackerService {
  private readonly apiUrl = `${environment.apiUrl}/health`;

  constructor(private http: HttpClient) {}

  // ==================== CRUD Operations ====================

  createReading(reading: CreateHealthReading): Observable<HealthReading> {
    return this.http.post<HealthReading>(`${this.apiUrl}/readings`, reading);
  }

  getReading(id: number): Observable<HealthReading> {
    return this.http.get<HealthReading>(`${this.apiUrl}/readings/${id}`);
  }

  updateReading(id: number, reading: CreateHealthReading): Observable<HealthReading> {
    return this.http.put<HealthReading>(`${this.apiUrl}/readings/${id}`, reading);
  }

  deleteReading(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/readings/${id}`);
  }

  // ==================== List Operations ====================

  getReadings(
    familyMemberId?: number,
    type?: ReadingType,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<HealthReading>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    if (type) {
      params = params.set('type', type);
    }

    return this.http.get<PagedResponse<HealthReading>>(`${this.apiUrl}/readings`, { params });
  }

  getReadingsByDateRange(
    startDate: Date,
    endDate: Date,
    familyMemberId?: number,
    type?: ReadingType
  ): Observable<HealthReading[]> {
    let params = new HttpParams()
      .set('startDate', startDate.toISOString())
      .set('endDate', endDate.toISOString());

    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    if (type) {
      params = params.set('type', type);
    }

    return this.http.get<HealthReading[]>(`${this.apiUrl}/readings/range`, { params });
  }

  // ==================== Statistics ====================

  getStats(type: ReadingType, familyMemberId?: number, days: number = 30): Observable<HealthStats> {
    let params = new HttpParams().set('days', days.toString());
    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    return this.http.get<HealthStats>(`${this.apiUrl}/stats/${type}`, { params });
  }

  getDashboardSummary(familyMemberId?: number): Observable<DashboardSummary> {
    let params = new HttpParams();
    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    return this.http.get<DashboardSummary>(`${this.apiUrl}/dashboard`, { params });
  }

  getChartData(type: ReadingType, familyMemberId?: number, days: number = 30): Observable<ChartDataPoint[]> {
    let params = new HttpParams().set('days', days.toString());
    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }
    return this.http.get<ChartDataPoint[]>(`${this.apiUrl}/chart/${type}`, { params });
  }

  // ==================== Alerts ====================

  getAlerts(unacknowledgedOnly: boolean = false): Observable<HealthAlert[]> {
    const params = new HttpParams().set('unacknowledgedOnly', unacknowledgedOnly.toString());
    return this.http.get<HealthAlert[]>(`${this.apiUrl}/alerts`, { params });
  }

  acknowledgeAlert(alertId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/alerts/${alertId}/acknowledge`, {});
  }

  getUnacknowledgedAlertsCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/alerts/count`);
  }

  // ==================== Reading Types ====================

  getReadingTypes(): Observable<ReadingTypeInfo[]> {
    return this.http.get<ReadingTypeInfo[]>(`${this.apiUrl}/types`);
  }
}
