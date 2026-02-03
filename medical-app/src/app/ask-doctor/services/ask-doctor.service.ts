import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  Conversation,
  ChatMessage,
  CreateConversationDto,
  SendMessageDto,
  AssignedDoctor,
  PagedResponse,
  ConversationStatus
} from '../models/ask-doctor.model';

@Injectable({
  providedIn: 'root'
})
export class AskDoctorService {
  private readonly apiUrl = `${environment.apiUrl}/conversations`;

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ==================== Conversation Methods ====================

  createConversation(dto: CreateConversationDto): Observable<Conversation> {
    return this.http.post<Conversation>(this.apiUrl, dto);
  }

  getConversation(id: number): Observable<Conversation> {
    return this.http.get<Conversation>(`${this.apiUrl}/${id}`);
  }

  getPatientConversations(
    status?: ConversationStatus,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<Conversation>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<PagedResponse<Conversation>>(`${this.apiUrl}/patient`, { params });
  }

  getDoctorConversations(
    status?: ConversationStatus,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<Conversation>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<PagedResponse<Conversation>>(`${this.apiUrl}/doctor`, { params });
  }

  updateConversationStatus(id: number, status: ConversationStatus): Observable<Conversation> {
    return this.http.patch<Conversation>(`${this.apiUrl}/${id}/status`, { status });
  }

  getUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/unread-count`).pipe(
      tap(res => this.unreadCountSubject.next(res.unreadCount))
    );
  }

  // ==================== Message Methods ====================

  sendMessage(conversationId: number, dto: SendMessageDto): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.apiUrl}/${conversationId}/messages`, dto);
  }

  sendMessageWithAttachment(conversationId: number, content: string | null, file: File): Observable<ChatMessage> {
    const formData = new FormData();
    formData.append('file', file);
    if (content) {
      formData.append('content', content);
    }
    return this.http.post<ChatMessage>(`${this.apiUrl}/${conversationId}/messages/attachment`, formData);
  }

  getMessages(conversationId: number, page: number = 0, size: number = 50): Observable<PagedResponse<ChatMessage>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ChatMessage>>(`${this.apiUrl}/${conversationId}/messages`, { params });
  }

  getNewMessages(conversationId: number, since: string): Observable<ChatMessage[]> {
    const params = new HttpParams().set('since', since);
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/${conversationId}/messages/new`, { params });
  }

  markAsRead(conversationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${conversationId}/read`, {});
  }

  // ==================== Assigned Doctors ====================

  getAssignedDoctors(): Observable<AssignedDoctor[]> {
    return this.http.get<AssignedDoctor[]>(`${this.apiUrl}/assigned-doctors`);
  }
}
