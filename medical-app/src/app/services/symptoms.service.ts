import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class SymptomsService {
  private baseUrl = 'http://localhost:8000'; // Update with your backend URL

  constructor(private http: HttpClient) {}

  // Fetch symptoms from the backend
  getSymptoms(): Observable<string[]> {
    return this.http.get<{ symptoms: string[] }>(`${this.baseUrl}/symptoms`).pipe(
      map((response) => response.symptoms) // Extract the symptoms array

    );
  }

  predictDisease(userId: string, symptoms: string[]): Observable<any> {
    const body = { user_id: userId, message: symptoms.join(', ') };
    return this.http.post(`${this.baseUrl}/predict`, body);
  }
  
}
