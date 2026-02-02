import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { DocumentService } from './services/document.service';
import { FamilyService } from '../services/family.service';
import {
  Document,
  DocumentFolder,
  DocumentType,
  DocumentStats,
  UploadDocumentDto,
  DOCUMENT_TYPES,
  getDocumentTypeInfo
} from './models/document.model';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './documents.component.html'
})
export class DocumentsComponent implements OnInit, OnDestroy {
  // Data
  documents: Document[] = [];
  folders: DocumentFolder[] = [];
  folderTree: DocumentFolder[] = [];
  stats: DocumentStats | null = null;
  recentDocuments: Document[] = [];

  // UI State
  isLoading = false;
  viewMode: 'grid' | 'list' = 'grid';
  selectedFolderId: number | null = null;
  selectedTypeFilter: DocumentType | '' = '';
  searchQuery = '';

  // Pagination
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 20;

  // Modals
  showUploadModal = false;
  showFolderModal = false;
  showPreviewModal = false;
  showDeleteConfirm = false;

  // Upload form
  uploadForm: UploadDocumentDto = {
    documentType: 'OTHER',
    title: '',
    description: '',
    tags: [],
    shareWithDoctor: false
  };
  selectedFile: File | null = null;
  uploadProgress = 0;
  isUploading = false;
  tagInput = '';

  // Folder form
  folderForm: Partial<DocumentFolder> = { name: '' };
  editingFolderId: number | null = null;

  // Preview
  previewDocument: Document | null = null;

  // Delete
  documentToDelete: Document | null = null;

  // Constants
  documentTypes = DOCUMENT_TYPES;

  // Family member context
  private profileSubscription?: Subscription;
  activeFamilyMemberId: number | null = null;
  activeMemberName: string | null = null;

  constructor(
    private documentService: DocumentService,
    private familyService: FamilyService
  ) {}

  ngOnInit(): void {
    // Subscribe to profile changes to reload data when family member changes
    this.profileSubscription = this.familyService.activeProfile$.subscribe(profile => {
      const newMemberId = profile && !profile.isOwnProfile ? profile.familyMemberId : null;
      const memberChanged = this.activeFamilyMemberId !== newMemberId;

      this.activeFamilyMemberId = newMemberId;
      this.activeMemberName = profile && !profile.isOwnProfile ? profile.name : null;

      // Reload data if member changed
      if (memberChanged) {
        this.loadData();
      }
    });

    // Initial load if no profile subscription triggered yet
    if (!this.documents.length) {
      this.loadData();
    }
  }

  ngOnDestroy(): void {
    this.profileSubscription?.unsubscribe();
  }

  loadData(): void {
    this.loadDocuments();
    this.loadFolders();
    this.loadStats();
    this.loadRecentDocuments();
  }

  loadDocuments(): void {
    this.isLoading = true;
    this.documentService.getDocuments(
      this.selectedFolderId ?? undefined,
      this.selectedTypeFilter || undefined,
      this.activeFamilyMemberId ?? undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response) => {
        this.documents = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.isLoading = false;
      }
    });
  }

  loadFolders(): void {
    this.documentService.getFolderTree().subscribe({
      next: (folders) => {
        this.folderTree = folders;
        this.folders = this.flattenFolders(folders);
      },
      error: (error) => console.error('Error loading folders:', error)
    });
  }

  loadStats(): void {
    this.documentService.getStats().subscribe({
      next: (stats) => this.stats = stats,
      error: (error) => console.error('Error loading stats:', error)
    });
  }

  loadRecentDocuments(): void {
    this.documentService.getRecentDocuments().subscribe({
      next: (docs) => this.recentDocuments = docs.slice(0, 5),
      error: (error) => console.error('Error loading recent documents:', error)
    });
  }

  flattenFolders(folders: DocumentFolder[]): DocumentFolder[] {
    let result: DocumentFolder[] = [];
    for (const folder of folders) {
      result.push(folder);
      if (folder.subFolders && folder.subFolders.length > 0) {
        result = result.concat(this.flattenFolders(folder.subFolders));
      }
    }
    return result;
  }

  // ==================== Filter & Search ====================

  onFolderChange(): void {
    this.currentPage = 0;
    this.loadDocuments();
  }

  onTypeFilterChange(): void {
    this.currentPage = 0;
    this.loadDocuments();
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.loadDocuments();
      return;
    }

    this.isLoading = true;
    this.documentService.searchDocuments(this.searchQuery, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.documents = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error searching documents:', error);
        this.isLoading = false;
      }
    });
  }

  clearFilters(): void {
    this.selectedFolderId = null;
    this.selectedTypeFilter = '';
    this.searchQuery = '';
    this.currentPage = 0;
    this.loadDocuments();
  }

  // ==================== Pagination ====================

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadDocuments();
    }
  }

  // ==================== Upload ====================

  openUploadModal(): void {
    this.uploadForm = {
      documentType: 'OTHER',
      title: '',
      description: '',
      tags: [],
      folderId: this.selectedFolderId ?? undefined,
      familyMemberId: this.activeFamilyMemberId ?? undefined,
      shareWithDoctor: false
    };
    this.selectedFile = null;
    this.uploadProgress = 0;
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
    this.selectedFile = null;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      if (!this.uploadForm.title) {
        this.uploadForm.title = this.selectedFile.name.replace(/\.[^/.]+$/, '');
      }
    }
  }

  addTag(): void {
    const tag = this.tagInput.trim();
    if (tag && !this.uploadForm.tags?.includes(tag)) {
      if (!this.uploadForm.tags) this.uploadForm.tags = [];
      this.uploadForm.tags.push(tag);
      this.tagInput = '';
    }
  }

  removeTag(tag: string): void {
    if (this.uploadForm.tags) {
      this.uploadForm.tags = this.uploadForm.tags.filter(t => t !== tag);
    }
  }

  uploadDocument(): void {
    if (!this.selectedFile || !this.uploadForm.title) return;

    this.isUploading = true;
    this.documentService.uploadDocument(this.selectedFile, this.uploadForm).subscribe({
      next: () => {
        this.isUploading = false;
        this.closeUploadModal();
        this.loadData();
      },
      error: (error) => {
        console.error('Error uploading document:', error);
        this.isUploading = false;
        alert('Failed to upload document. Please try again.');
      }
    });
  }

  // ==================== Folders ====================

  openFolderModal(folder?: DocumentFolder): void {
    if (folder) {
      this.editingFolderId = folder.id;
      this.folderForm = { name: folder.name, icon: folder.icon, color: folder.color };
    } else {
      this.editingFolderId = null;
      this.folderForm = { name: '', parentFolderId: this.selectedFolderId ?? undefined };
    }
    this.showFolderModal = true;
  }

  closeFolderModal(): void {
    this.showFolderModal = false;
    this.editingFolderId = null;
  }

  saveFolder(): void {
    if (!this.folderForm.name?.trim()) return;

    const action = this.editingFolderId
      ? this.documentService.updateFolder(this.editingFolderId, this.folderForm)
      : this.documentService.createFolder(this.folderForm);

    action.subscribe({
      next: () => {
        this.closeFolderModal();
        this.loadFolders();
        this.loadStats();
      },
      error: (error) => {
        console.error('Error saving folder:', error);
        alert('Failed to save folder. Please try again.');
      }
    });
  }

  deleteFolder(folder: DocumentFolder): void {
    if (!confirm(`Delete folder "${folder.name}" and all its contents?`)) return;

    this.documentService.deleteFolder(folder.id).subscribe({
      next: () => {
        if (this.selectedFolderId === folder.id) {
          this.selectedFolderId = null;
        }
        this.loadData();
      },
      error: (error) => {
        console.error('Error deleting folder:', error);
        alert('Failed to delete folder. Please try again.');
      }
    });
  }

  // ==================== Document Actions ====================

  previewDoc(doc: Document): void {
    this.previewDocument = doc;
    this.showPreviewModal = true;
  }

  closePreview(): void {
    this.showPreviewModal = false;
    this.previewDocument = null;
  }

  downloadDoc(doc: Document): void {
    this.documentService.downloadDocument(doc.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = doc.fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading document:', error);
        alert('Failed to download document.');
      }
    });
  }

  toggleShare(doc: Document): void {
    this.documentService.toggleShareWithDoctor(doc.id).subscribe({
      next: (updated) => {
        const index = this.documents.findIndex(d => d.id === doc.id);
        if (index !== -1) {
          this.documents[index] = updated;
        }
      },
      error: (error) => console.error('Error toggling share:', error)
    });
  }

  confirmDelete(doc: Document): void {
    this.documentToDelete = doc;
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.documentToDelete = null;
  }

  deleteDoc(): void {
    if (!this.documentToDelete) return;

    this.documentService.deleteDocument(this.documentToDelete.id).subscribe({
      next: () => {
        this.cancelDelete();
        this.loadData();
      },
      error: (error) => {
        console.error('Error deleting document:', error);
        alert('Failed to delete document.');
      }
    });
  }

  // ==================== Helpers ====================

  getTypeInfo(type: DocumentType) {
    return getDocumentTypeInfo(type);
  }

  getDocumentIcon(doc: Document): string {
    if (doc.isImage) return 'image';
    if (doc.isPdf) return 'file-pdf';
    const ext = doc.fileExtension.toLowerCase();
    if (['doc', 'docx'].includes(ext)) return 'file-word';
    if (['xls', 'xlsx'].includes(ext)) return 'file-excel';
    return 'file';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('ro-RO', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  getThumbnailUrl(doc: Document): string {
    if (doc.thumbnailUrl) {
      return `http://localhost:8080${doc.thumbnailUrl}`;
    }
    return '';
  }

  getPreviewUrl(doc: Document): string {
    // For images, use the download endpoint directly
    // For PDFs, use the download endpoint
    return `http://localhost:8080/api/documents/${doc.id}/download`;
  }
}
