export type ConversationStatus = 'ACTIVE' | 'CLOSED' | 'ARCHIVED';
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE';

export interface Conversation {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  doctorSpecialization: string;
  familyMemberId?: number;
  familyMemberName?: string;
  subject: string;
  status: ConversationStatus;
  lastMessageAt?: string;
  lastMessagePreview?: string;
  unreadCount: number;
  createdAt: string;
}

export interface ChatMessage {
  id: number;
  conversationId: number;
  senderId: number;
  senderType: 'PATIENT' | 'DOCTOR';
  senderName: string;
  messageType: MessageType;
  content: string;
  attachmentUrl?: string;
  attachmentName?: string;
  attachmentSize?: number;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

export interface CreateConversationDto {
  doctorId: number;
  familyMemberId?: number;
  subject: string;
  initialMessage: string;
}

export interface SendMessageDto {
  conversationId?: number;
  content: string;
  messageType?: MessageType;
}

export interface AssignedDoctor {
  id: number;
  name: string;
  email: string;
  specialization: string;
  hospital: string;
  rating?: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
