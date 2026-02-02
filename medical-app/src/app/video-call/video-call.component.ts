import { Component, OnInit, OnDestroy } from '@angular/core';
import { VideoCallMessageService, VideoCallMessageDto } from '../services/video-call-message.service';

interface ChatMessage {
  sender: string;
  message: string;
  timestamp: Date;
}

interface ConnectionQuality {
  level: 'excellent' | 'good' | 'poor' | 'unknown';
  bitrate: number;
  packetsLost: number;
}

interface SharedFile {
  name: string;
  size: number;
  type: string;
  url: string;
  sender: string;
  timestamp: Date;
}

@Component({
  selector: 'app-video-call',
  templateUrl: './video-call.component.html'
})
export class VideoCallComponent implements OnInit, OnDestroy {
  constructor(private videoCallMessageService: VideoCallMessageService) {}

  localStream: MediaStream | null = null;
  remoteStream: MediaStream | null = null;
  peerConnection: RTCPeerConnection | null = null;
  socket: WebSocket | null = null;
  room: string = 'default';

  // Premium Features
  chatMessages: ChatMessage[] = [];
  newMessage: string = '';
  isChatOpen: boolean = false;

  isAudioMuted: boolean = false;
  isVideoOff: boolean = false;
  isRecording: boolean = false;
  isScreenSharing: boolean = false;

  connectionQuality: ConnectionQuality = {
    level: 'unknown',
    bitrate: 0,
    packetsLost: 0
  };

  qualityCheckInterval: any;
  username: string = 'User' + Math.floor(Math.random() * 1000);
  userId: number | null = null;

  // Screen sharing
  screenStream: MediaStream | null = null;
  originalVideoTrack: MediaStreamTrack | null = null;

  // Recording
  mediaRecorder: MediaRecorder | null = null;
  recordedChunks: Blob[] = [];

  // File sharing
  sharedFiles: SharedFile[] = [];
  isFilesPanelOpen: boolean = false;

  async ngOnInit() {
    // Get user info from localStorage
    const userJson = localStorage.getItem('user');
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.userId = user.id;
        this.username = user.name || 'User' + Math.floor(Math.random() * 1000);
      } catch (e) {
        console.error('Error parsing user from localStorage:', e);
      }
    }

    this.socket = new WebSocket('ws://localhost:3000');

    // Ascultă mesajele de la server
    this.socket.onmessage = async (message) => {
      try {
        const data = JSON.parse(message.data);

        switch (data.type) {
          case 'offer':
            console.log('Received offer:', data);
            await this.peerConnection?.setRemoteDescription(new RTCSessionDescription(data));
            const answer = await this.peerConnection?.createAnswer();
            await this.peerConnection?.setLocalDescription(answer);
            this.socket?.send(JSON.stringify({ ...answer, type: 'answer' }));
            break;

          case 'answer':
            console.log('Received answer:', data);
            await this.peerConnection?.setRemoteDescription(new RTCSessionDescription(data));
            break;

          case 'candidate':
            console.log('Received ICE candidate:', data.candidate);
            if (data.candidate) {
              await this.peerConnection?.addIceCandidate(new RTCIceCandidate(data.candidate));
            }
            break;

          case 'chat':
            // Handle incoming chat messages
            this.chatMessages.push({
              sender: data.sender || 'Remote User',
              message: data.message,
              timestamp: new Date()
            });
            break;

          case 'file':
            // Handle incoming file
            this.sharedFiles.push({
              name: data.name,
              size: data.size,
              type: data.type,
              url: data.url,
              sender: data.sender || 'Remote User',
              timestamp: new Date()
            });
            break;

          default:
            console.error('Unknown message type:', data.type);
        }
      } catch (error) {
        console.error('Error processing WebSocket message:', error);
      }
    };
    
    // Alătură-te camerei implicite
    this.socket.onopen = () => {
      this.socket?.send(JSON.stringify({ type: 'join', room: this.room }));
    };
  }

  async startCall() {
    // Creează conexiunea Peer-to-Peer
    this.peerConnection = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
    });

    // Adaugă fluxul local
    this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    const localVideo = document.getElementById('localVideo') as HTMLVideoElement;
    localVideo.srcObject = this.localStream;

    this.localStream.getTracks().forEach((track) => {
      this.peerConnection?.addTrack(track, this.localStream!);
    });

    // Gestionează fluxul remote
    this.remoteStream = new MediaStream();
    const remoteVideo = document.getElementById('remoteVideo') as HTMLVideoElement;
    remoteVideo.srcObject = this.remoteStream;

    this.peerConnection.ontrack = (event) => {
      this.remoteStream?.addTrack(event.track);
    };

    // Gestionează candidații ICE
    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        this.socket?.send(
          JSON.stringify({ type: 'candidate', candidate: event.candidate.toJSON() })
        );
      }
    };

    const offer = await this.peerConnection.createOffer();
    await this.peerConnection.setLocalDescription(offer);

    // Trimite oferta către serverul de semnalizare
    this.socket?.send(JSON.stringify({ ...offer, type: 'offer' }));

    // Start monitoring connection quality
    this.startQualityMonitoring();
  }

  /**
   * Toggle audio mute/unmute
   */
  toggleAudio(): void {
    if (this.localStream) {
      const audioTrack = this.localStream.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        this.isAudioMuted = !audioTrack.enabled;
      }
    }
  }

  /**
   * Toggle video on/off
   */
  toggleVideo(): void {
    if (this.localStream) {
      const videoTrack = this.localStream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
        this.isVideoOff = !videoTrack.enabled;
      }
    }
  }

  /**
   * Toggle recording with MediaRecorder API
   */
  toggleRecording(): void {
    if (this.isRecording) {
      this.stopRecording();
    } else {
      this.startRecording();
    }
  }

  /**
   * Start recording the call
   */
  private startRecording(): void {
    if (!this.localStream && !this.remoteStream) {
      console.error('No streams available for recording');
      return;
    }

    try {
      // Create a combined stream for recording
      const audioContext = new AudioContext();
      const destination = audioContext.createMediaStreamDestination();

      // Add local audio if available
      if (this.localStream) {
        const localAudio = audioContext.createMediaStreamSource(this.localStream);
        localAudio.connect(destination);
      }

      // Add remote audio if available
      if (this.remoteStream && this.remoteStream.getAudioTracks().length > 0) {
        const remoteAudio = audioContext.createMediaStreamSource(this.remoteStream);
        remoteAudio.connect(destination);
      }

      // Get video track (local or screen share)
      const videoTrack = this.localStream?.getVideoTracks()[0];

      // Create combined stream
      const combinedStream = new MediaStream();
      if (videoTrack) {
        combinedStream.addTrack(videoTrack);
      }
      destination.stream.getAudioTracks().forEach(track => combinedStream.addTrack(track));

      // Configure MediaRecorder
      const options: MediaRecorderOptions = { mimeType: 'video/webm;codecs=vp9,opus' };
      if (!MediaRecorder.isTypeSupported(options.mimeType!)) {
        options.mimeType = 'video/webm;codecs=vp8,opus';
      }
      if (!MediaRecorder.isTypeSupported(options.mimeType!)) {
        options.mimeType = 'video/webm';
      }

      this.mediaRecorder = new MediaRecorder(combinedStream, options);
      this.recordedChunks = [];

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.recordedChunks.push(event.data);
        }
      };

      this.mediaRecorder.onstop = () => {
        this.saveRecording();
      };

      this.mediaRecorder.start(1000); // Collect data every second
      this.isRecording = true;
      console.log('Recording started');
    } catch (error) {
      console.error('Error starting recording:', error);
    }
  }

  /**
   * Stop recording
   */
  private stopRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
      this.isRecording = false;
      console.log('Recording stopped');
    }
  }

  /**
   * Save the recorded video
   */
  private saveRecording(): void {
    if (this.recordedChunks.length === 0) return;

    const blob = new Blob(this.recordedChunks, { type: 'video/webm' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `call-recording-${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.webm`;
    link.click();
    URL.revokeObjectURL(url);

    this.recordedChunks = [];
  }

  /**
   * Toggle chat panel
   */
  toggleChat(): void {
    this.isChatOpen = !this.isChatOpen;
    this.isFilesPanelOpen = false;
  }

  // ==================== SCREEN SHARING ====================

  /**
   * Toggle screen sharing
   */
  async toggleScreenShare(): Promise<void> {
    if (this.isScreenSharing) {
      await this.stopScreenShare();
    } else {
      await this.startScreenShare();
    }
  }

  /**
   * Start screen sharing
   */
  private async startScreenShare(): Promise<void> {
    try {
      // Get screen share stream
      this.screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          cursor: 'always'
        } as any,
        audio: false
      });

      // Save original video track
      if (this.localStream) {
        this.originalVideoTrack = this.localStream.getVideoTracks()[0];
      }

      // Replace video track in peer connection
      const screenTrack = this.screenStream.getVideoTracks()[0];
      const sender = this.peerConnection?.getSenders().find(s => s.track?.kind === 'video');

      if (sender) {
        await sender.replaceTrack(screenTrack);
      }

      // Update local video display
      const localVideo = document.getElementById('localVideo') as HTMLVideoElement;
      if (localVideo) {
        localVideo.srcObject = this.screenStream;
      }

      // Listen for when user stops sharing via browser UI
      screenTrack.onended = () => {
        this.stopScreenShare();
      };

      this.isScreenSharing = true;
      console.log('Screen sharing started');
    } catch (error) {
      console.error('Error starting screen share:', error);
    }
  }

  /**
   * Stop screen sharing and restore camera
   */
  private async stopScreenShare(): Promise<void> {
    try {
      // Stop screen stream
      this.screenStream?.getTracks().forEach(track => track.stop());

      // Restore original video track
      if (this.originalVideoTrack && this.peerConnection) {
        const sender = this.peerConnection.getSenders().find(s => s.track?.kind === 'video');
        if (sender) {
          await sender.replaceTrack(this.originalVideoTrack);
        }

        // Restore local video display
        const localVideo = document.getElementById('localVideo') as HTMLVideoElement;
        if (localVideo && this.localStream) {
          localVideo.srcObject = this.localStream;
        }
      }

      this.screenStream = null;
      this.isScreenSharing = false;
      console.log('Screen sharing stopped');
    } catch (error) {
      console.error('Error stopping screen share:', error);
    }
  }

  // ==================== FILE SHARING ====================

  /**
   * Toggle files panel
   */
  toggleFilesPanel(): void {
    this.isFilesPanelOpen = !this.isFilesPanelOpen;
    this.isChatOpen = false;
  }

  /**
   * Handle file selection for sharing
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }

      this.shareFile(file);
      input.value = ''; // Reset input
    }
  }

  /**
   * Share a file
   */
  private shareFile(file: File): void {
    const reader = new FileReader();

    reader.onload = () => {
      const fileUrl = reader.result as string;

      const sharedFile: SharedFile = {
        name: file.name,
        size: file.size,
        type: file.type,
        url: fileUrl,
        sender: this.username,
        timestamp: new Date()
      };

      // Add to local list
      this.sharedFiles.push(sharedFile);

      // Send via WebSocket (type is 'file' for routing, fileType is MIME type)
      if (this.socket) {
        this.socket.send(JSON.stringify({
          type: 'file',
          name: sharedFile.name,
          size: sharedFile.size,
          fileType: sharedFile.type,
          url: sharedFile.url,
          sender: sharedFile.sender
        }));
      }
    };

    reader.readAsDataURL(file);
  }

  /**
   * Download a shared file
   */
  downloadFile(file: SharedFile): void {
    const link = document.createElement('a');
    link.href = file.url;
    link.download = file.name;
    link.click();
  }

  /**
   * Format file size for display
   */
  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  /**
   * Send chat message
   */
  sendMessage(): void {
    if (this.newMessage.trim() && this.socket) {
      const chatMessage: ChatMessage = {
        sender: this.username,
        message: this.newMessage.trim(),
        timestamp: new Date()
      };

      // Add to local chat
      this.chatMessages.push(chatMessage);

      // Send via WebSocket
      this.socket.send(JSON.stringify({
        type: 'chat',
        sender: this.username,
        message: this.newMessage.trim()
      }));

      this.newMessage = '';
    }
  }

  /**
   * Start monitoring connection quality
   */
  startQualityMonitoring(): void {
    this.qualityCheckInterval = setInterval(async () => {
      if (this.peerConnection) {
        const stats = await this.peerConnection.getStats();
        let bitrate = 0;
        let packetsLost = 0;

        stats.forEach((report) => {
          if (report.type === 'inbound-rtp' && report.mediaType === 'video') {
            bitrate = report.bytesReceived ? report.bytesReceived * 8 / 1000 : 0; // kbps
            packetsLost = report.packetsLost || 0;
          }
        });

        // Determine quality level
        let level: 'excellent' | 'good' | 'poor' = 'good';
        if (bitrate > 1000 && packetsLost < 10) {
          level = 'excellent';
        } else if (bitrate < 500 || packetsLost > 50) {
          level = 'poor';
        }

        this.connectionQuality = { level, bitrate, packetsLost };
      }
    }, 2000); // Check every 2 seconds
  }

  endCall() {
    // Stop recording if active
    if (this.isRecording && this.mediaRecorder) {
      this.stopRecording();
    }

    // Save chat messages to database before ending call
    this.saveChatMessagesToDb();

    // Stop screen sharing if active
    this.screenStream?.getTracks().forEach((track) => track.stop());
    this.screenStream = null;

    this.localStream?.getTracks().forEach((track) => track.stop());
    this.peerConnection?.close();
    this.localStream = null;
    this.remoteStream = null;
    this.peerConnection = null;

    this.socket?.close();
    this.socket = null;

    // Stop quality monitoring
    if (this.qualityCheckInterval) {
      clearInterval(this.qualityCheckInterval);
    }

    // Reset states
    this.isRecording = false;
    this.isAudioMuted = false;
    this.isVideoOff = false;
    this.isScreenSharing = false;
    this.chatMessages = [];
    this.sharedFiles = [];
    this.originalVideoTrack = null;
  }

  /**
   * Save chat messages to database
   */
  private saveChatMessagesToDb(): void {
    if (this.chatMessages.length === 0) {
      console.log('No chat messages to save');
      return;
    }

    const messagesToSave: VideoCallMessageDto[] = this.chatMessages.map(msg => ({
      roomId: this.room,
      senderName: msg.sender,
      senderId: msg.sender === this.username ? this.userId ?? undefined : undefined,
      senderType: msg.sender === this.username ? 'PATIENT' : 'REMOTE',
      message: msg.message,
      createdAt: msg.timestamp.toISOString()
    }));

    this.videoCallMessageService.saveMessages(messagesToSave).subscribe({
      next: (saved) => {
        console.log(`Saved ${saved.length} chat messages to database`);
      },
      error: (err) => {
        console.error('Error saving chat messages:', err);
      }
    });
  }

  ngOnDestroy(): void {
    this.endCall();
  }

  /**
   * Get quality indicator color
   */
  getQualityColor(): string {
    switch (this.connectionQuality.level) {
      case 'excellent':
        return 'text-green-500';
      case 'good':
        return 'text-yellow-500';
      case 'poor':
        return 'text-red-500';
      default:
        return 'text-gray-500';
    }
  }
}
