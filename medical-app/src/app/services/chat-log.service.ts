import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatLogDto {
  id?: number;
  prognosis: string;
  description?: string;
  precautions?: string[];
  complications?: string[];
  severity?: number;
  symptoms?: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ChatLogService {
  private baseUrl = 'http://localhost:8080/api/chat-logs';

  constructor(private http: HttpClient) {}

  /**
   * Get all chat logs for the current user
   */
  getAllChatLogs(): Observable<ChatLogDto[]> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<ChatLogDto[]>(this.baseUrl, { headers });
  }

  /**
   * Save a new chat log (AI prediction result) to the backend
   */
  saveChatLog(chatLog: ChatLogDto): Observable<string> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
    return this.http.post<string>(`${this.baseUrl}/save`, chatLog, { headers, responseType: 'text' as 'json' });
  }
}
