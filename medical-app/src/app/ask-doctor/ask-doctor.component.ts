import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AskDoctorService } from './services/ask-doctor.service';
import { FamilyService } from '../services/family.service';
import { AuthService } from '../services/auth.service';
import {
  Conversation,
  ChatMessage,
  CreateConversationDto,
  SendMessageDto,
  AssignedDoctor,
  ConversationStatus
} from './models/ask-doctor.model';

@Component({
  selector: 'app-ask-doctor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ask-doctor.component.html'
})
export class AskDoctorComponent implements OnInit, OnDestroy {
  @ViewChild('messageContainer') messageContainer!: ElementRef;
  @ViewChild('fileInput') fileInput!: ElementRef;

  // Data
  conversations: Conversation[] = [];
  messages: ChatMessage[] = [];
  assignedDoctors: AssignedDoctor[] = [];

  // Active state
  activeConversation: Conversation | null = null;
  activeFamilyMemberId: number | null = null;
  activeMemberName: string | null = null;

  // UI State
  isLoading = false;
  isLoadingMessages = false;
  isSending = false;
  statusFilter: ConversationStatus | '' = '';

  // Pagination
  currentPage = 0;
  totalPages = 0;
  pageSize = 20;

  // New conversation modal
  showNewConversationModal = false;
  selectedDoctorId: number | null = null;
  newSubject = '';
  newInitialMessage = '';

  // Chat input
  messageInput = '';
  selectedFile: File | null = null;

  // Alert
  showAlert = false;
  alertMsg = '';
  alertColor = 'green';

  // Polling
  private pollingSubscription?: Subscription;
  private profileSubscription?: Subscription;
  private userSubscription?: Subscription;
  private lastMessageTime?: string;

  // User info
  isPatient = false;
  isDoctor = false;

  constructor(
    private askDoctorService: AskDoctorService,
    private familyService: FamilyService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userSubscription = this.authService.user$.subscribe(user => {
      if (user) {
        this.isPatient = user.role === 'PATIENT';
        this.isDoctor = user.role === 'DOCTOR';
        this.loadConversations();

        if (this.isPatient) {
          this.loadAssignedDoctors();
        }
      }
    });

    this.profileSubscription = this.familyService.activeProfile$.subscribe(profile => {
      this.activeFamilyMemberId = profile && !profile.isOwnProfile ? profile.familyMemberId : null;
      this.activeMemberName = profile && !profile.isOwnProfile ? profile.name : null;
    });
  }

  ngOnDestroy(): void {
    this.stopPolling();
    this.profileSubscription?.unsubscribe();
    this.userSubscription?.unsubscribe();
  }

  // ==================== Data Loading ====================

  loadConversations(): void {
    this.isLoading = true;
    const status = this.statusFilter || undefined;

    const request = this.isPatient
      ? this.askDoctorService.getPatientConversations(status, this.currentPage, this.pageSize)
      : this.askDoctorService.getDoctorConversations(status, this.currentPage, this.pageSize);

    request.subscribe({
      next: (response) => {
        this.conversations = response.content;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
        this.isLoading = false;
        this.showAlertMessage('Failed to load conversations', 'red');
      }
    });
  }

  loadMessages(): void {
    if (!this.activeConversation) return;

    this.isLoadingMessages = true;
    this.askDoctorService.getMessages(this.activeConversation.id).subscribe({
      next: (response) => {
        this.messages = response.content.reverse();
        this.isLoadingMessages = false;
        this.scrollToBottom();
        this.markConversationAsRead();

        if (this.messages.length > 0) {
          this.lastMessageTime = this.messages[this.messages.length - 1].createdAt;
        }

        this.startPolling();
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.isLoadingMessages = false;
      }
    });
  }

  loadAssignedDoctors(): void {
    this.askDoctorService.getAssignedDoctors().subscribe({
      next: (doctors) => this.assignedDoctors = doctors,
      error: (error) => console.error('Error loading assigned doctors:', error)
    });
  }

  // ==================== Conversation Actions ====================

  selectConversation(conversation: Conversation): void {
    this.activeConversation = conversation;
    this.loadMessages();
  }

  openNewConversationModal(): void {
    this.selectedDoctorId = null;
    this.newSubject = '';
    this.newInitialMessage = '';
    this.showNewConversationModal = true;
  }

  closeNewConversationModal(): void {
    this.showNewConversationModal = false;
  }

  createConversation(): void {
    if (!this.selectedDoctorId || !this.newSubject.trim() || !this.newInitialMessage.trim()) {
      this.showAlertMessage('Please fill all required fields', 'red');
      return;
    }

    const dto: CreateConversationDto = {
      doctorId: this.selectedDoctorId,
      familyMemberId: this.activeFamilyMemberId || undefined,
      subject: this.newSubject.trim(),
      initialMessage: this.newInitialMessage.trim()
    };

    this.askDoctorService.createConversation(dto).subscribe({
      next: (conversation) => {
        this.conversations.unshift(conversation);
        this.selectConversation(conversation);
        this.closeNewConversationModal();
        this.showAlertMessage('Conversation started successfully', 'green');
      },
      error: (error) => {
        console.error('Error creating conversation:', error);
        this.showAlertMessage('Failed to start conversation', 'red');
      }
    });
  }

  updateStatus(status: ConversationStatus): void {
    if (!this.activeConversation) return;

    this.askDoctorService.updateConversationStatus(this.activeConversation.id, status).subscribe({
      next: (updated) => {
        this.activeConversation = updated;
        const index = this.conversations.findIndex(c => c.id === updated.id);
        if (index !== -1) {
          this.conversations[index] = updated;
        }
        this.showAlertMessage(`Conversation ${status.toLowerCase()}`, 'green');
      },
      error: () => this.showAlertMessage('Failed to update status', 'red')
    });
  }

  // ==================== Message Actions ====================

  sendMessage(): void {
    if (!this.activeConversation || (!this.messageInput.trim() && !this.selectedFile)) return;

    this.isSending = true;

    if (this.selectedFile) {
      this.askDoctorService.sendMessageWithAttachment(
        this.activeConversation.id,
        this.messageInput.trim() || null,
        this.selectedFile
      ).subscribe({
        next: (message) => this.handleMessageSent(message),
        error: (error) => this.handleMessageError(error)
      });
    } else {
      const dto: SendMessageDto = {
        content: this.messageInput.trim()
      };

      this.askDoctorService.sendMessage(this.activeConversation.id, dto).subscribe({
        next: (message) => this.handleMessageSent(message),
        error: (error) => this.handleMessageError(error)
      });
    }
  }

  private handleMessageSent(message: ChatMessage): void {
    this.messages.push(message);
    this.messageInput = '';
    this.selectedFile = null;
    this.isSending = false;
    this.lastMessageTime = message.createdAt;
    this.scrollToBottom();

    if (this.activeConversation) {
      this.activeConversation.lastMessageAt = message.createdAt;
      this.activeConversation.lastMessagePreview = message.content.substring(0, 100);
      this.moveConversationToTop(this.activeConversation.id);
    }
  }

  private handleMessageError(error: any): void {
    console.error('Error sending message:', error);
    this.isSending = false;
    this.showAlertMessage('Failed to send message', 'red');
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      if (file.size > 10 * 1024 * 1024) {
        this.showAlertMessage('File size must be less than 10MB', 'red');
        return;
      }
      this.selectedFile = file;
    }
  }

  removeSelectedFile(): void {
    this.selectedFile = null;
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  // ==================== Polling ====================

  private startPolling(): void {
    this.stopPolling();
    if (!this.activeConversation || !this.lastMessageTime) return;

    this.pollingSubscription = interval(5000).pipe(
      switchMap(() => this.askDoctorService.getNewMessages(
        this.activeConversation!.id,
        this.lastMessageTime!
      ))
    ).subscribe({
      next: (newMessages) => {
        if (newMessages.length > 0) {
          const existingIds = new Set(this.messages.map(m => m.id));
          const uniqueMessages = newMessages.filter(m => !existingIds.has(m.id));

          if (uniqueMessages.length > 0) {
            this.messages.push(...uniqueMessages);
            this.lastMessageTime = uniqueMessages[uniqueMessages.length - 1].createdAt;
            this.scrollToBottom();
            this.markConversationAsRead();
          }
        }
      },
      error: (error) => console.error('Polling error:', error)
    });
  }

  private stopPolling(): void {
    this.pollingSubscription?.unsubscribe();
  }

  // ==================== Helpers ====================

  markConversationAsRead(): void {
    if (!this.activeConversation) return;

    this.askDoctorService.markAsRead(this.activeConversation.id).subscribe({
      next: () => {
        if (this.activeConversation) {
          this.activeConversation.unreadCount = 0;
          const index = this.conversations.findIndex(c => c.id === this.activeConversation?.id);
          if (index !== -1) {
            this.conversations[index].unreadCount = 0;
          }
        }
      }
    });
  }

  private moveConversationToTop(conversationId: number): void {
    const index = this.conversations.findIndex(c => c.id === conversationId);
    if (index > 0) {
      const conv = this.conversations.splice(index, 1)[0];
      this.conversations.unshift(conv);
    }
  }

  scrollToBottom(): void {
    setTimeout(() => {
      if (this.messageContainer) {
        const container = this.messageContainer.nativeElement;
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  }

  showAlertMessage(msg: string, color: string): void {
    this.alertMsg = msg;
    this.alertColor = color;
    this.showAlert = true;
    setTimeout(() => this.showAlert = false, 4000);
  }

  onStatusFilterChange(): void {
    this.currentPage = 0;
    this.loadConversations();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadConversations();
    }
  }

  backToList(): void {
    this.stopPolling();
    this.activeConversation = null;
    this.messages = [];
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (days === 0) {
      return date.toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' });
    } else if (days === 1) {
      return 'Yesterday';
    } else if (days < 7) {
      return date.toLocaleDateString('ro-RO', { weekday: 'short' });
    }
    return date.toLocaleDateString('ro-RO', { month: 'short', day: 'numeric' });
  }

  formatMessageTime(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' });
  }

  formatFullDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('ro-RO', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  isOwnMessage(message: ChatMessage): boolean {
    const userType = this.isPatient ? 'PATIENT' : 'DOCTOR';
    return message.senderType === userType;
  }

  getInitials(name: string): string {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getAttachmentUrl(url: string): string {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
  }

  isImageAttachment(message: ChatMessage): boolean {
    return message.messageType === 'IMAGE';
  }

  formatFileSize(bytes?: number): string {
    if (!bytes) return '';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  getStatusColor(status: ConversationStatus): string {
    switch (status) {
      case 'ACTIVE': return 'green';
      case 'CLOSED': return 'gray';
      case 'ARCHIVED': return 'yellow';
      default: return 'gray';
    }
  }
}
