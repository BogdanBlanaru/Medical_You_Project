import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PredictionService {
  private baseUrl = 'http://localhost:8000';

  constructor(private http: HttpClient) {}

  predictDisease(userId: string, symptoms: string[]): Observable<any> {
    const payload = {
      user_id: userId,
      message: symptoms.join(", "),
    };
    console.log('Sending payload:', payload);
    return this.http.post(`${this.baseUrl}/predict`, payload);
  }
  
}
