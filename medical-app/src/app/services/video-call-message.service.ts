import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VideoCallMessageDto {
  id?: number;
  roomId: string;
  senderName: string;
  senderId?: number;
  senderType?: string;
  message: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root',
})
export class VideoCallMessageService {
  private baseUrl = 'http://localhost:8080/api/video-call-messages';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Save a single chat message
   */
  saveMessage(message: VideoCallMessageDto): Observable<VideoCallMessageDto> {
    return this.http.post<VideoCallMessageDto>(this.baseUrl, message, {
      headers: this.getHeaders()
    });
  }

  /**
   * Save multiple messages at once (batch save at end of call)
   */
  saveMessages(messages: VideoCallMessageDto[]): Observable<VideoCallMessageDto[]> {
    return this.http.post<VideoCallMessageDto[]>(`${this.baseUrl}/batch`, messages, {
      headers: this.getHeaders()
    });
  }

  /**
   * Get all messages for a specific room/call
   */
  getMessagesByRoom(roomId: string): Observable<VideoCallMessageDto[]> {
    return this.http.get<VideoCallMessageDto[]>(`${this.baseUrl}/room/${roomId}`, {
      headers: this.getHeaders()
    });
  }

  /**
   * Get rooms that a user has participated in
   */
  getRoomsForUser(userId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/user/${userId}/rooms`, {
      headers: this.getHeaders()
    });
  }
}
