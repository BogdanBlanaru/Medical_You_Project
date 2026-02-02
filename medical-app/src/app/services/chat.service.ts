import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private baseUrl = 'http://localhost:8000';

  constructor(private http: HttpClient) {}

  getStartMessage(): Observable<{ message: string }> {
    return this.http.get<{ message: string }>(`${this.baseUrl}/start`);
  }

  sendUserResponse(userId: string, message: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/response`, {
      user_id: userId,
      message: message,
    });
  }

  getSymptoms(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/symptoms`);
  }
}
