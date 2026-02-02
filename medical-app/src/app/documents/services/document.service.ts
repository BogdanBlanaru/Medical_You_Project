import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Document,
  DocumentFolder,
  UploadDocumentDto,
  DocumentStats,
  PagedResponse,
  DocumentType
} from '../models/document.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly apiUrl = `${environment.apiUrl}/documents`;

  constructor(private http: HttpClient) {}

  // ==================== Document CRUD ====================

  uploadDocument(file: File, metadata: UploadDocumentDto): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', metadata.documentType);
    formData.append('title', metadata.title);

    if (metadata.description) {
      formData.append('description', metadata.description);
    }
    if (metadata.familyMemberId) {
      formData.append('familyMemberId', metadata.familyMemberId.toString());
    }
    if (metadata.folderId) {
      formData.append('folderId', metadata.folderId.toString());
    }
    if (metadata.tags && metadata.tags.length > 0) {
      metadata.tags.forEach(tag => formData.append('tags', tag));
    }
    if (metadata.documentDate) {
      formData.append('documentDate', metadata.documentDate);
    }
    if (metadata.shareWithDoctor !== undefined) {
      formData.append('shareWithDoctor', metadata.shareWithDoctor.toString());
    }

    return this.http.post<Document>(this.apiUrl, formData);
  }

  getDocument(id: number): Observable<Document> {
    return this.http.get<Document>(`${this.apiUrl}/${id}`);
  }

  updateDocument(id: number, dto: UploadDocumentDto): Observable<Document> {
    return this.http.put<Document>(`${this.apiUrl}/${id}`, dto);
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ==================== Download & Preview ====================

  downloadDocument(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, { responseType: 'blob' });
  }

  getDocumentThumbnailUrl(id: number): string {
    return `${this.apiUrl}/${id}/thumbnail`;
  }

  // ==================== Document Listing ====================

  getDocuments(
    folderId?: number,
    type?: DocumentType,
    familyMemberId?: number,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<Document>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (folderId !== undefined && folderId !== null) {
      params = params.set('folderId', folderId.toString());
    }
    if (type) {
      params = params.set('type', type);
    }
    if (familyMemberId) {
      params = params.set('familyMemberId', familyMemberId.toString());
    }

    return this.http.get<PagedResponse<Document>>(this.apiUrl, { params });
  }

  searchDocuments(query: string, page: number = 0, size: number = 20): Observable<PagedResponse<Document>> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Document>>(`${this.apiUrl}/search`, { params });
  }

  getRecentDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.apiUrl}/recent`);
  }

  getSharedDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.apiUrl}/shared`);
  }

  // ==================== Folder Operations ====================

  createFolder(folder: Partial<DocumentFolder>): Observable<DocumentFolder> {
    return this.http.post<DocumentFolder>(`${this.apiUrl}/folders`, folder);
  }

  getFolder(id: number): Observable<DocumentFolder> {
    return this.http.get<DocumentFolder>(`${this.apiUrl}/folders/${id}`);
  }

  updateFolder(id: number, folder: Partial<DocumentFolder>): Observable<DocumentFolder> {
    return this.http.put<DocumentFolder>(`${this.apiUrl}/folders/${id}`, folder);
  }

  deleteFolder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/folders/${id}`);
  }

  getFolders(): Observable<DocumentFolder[]> {
    return this.http.get<DocumentFolder[]>(`${this.apiUrl}/folders`);
  }

  getFolderTree(): Observable<DocumentFolder[]> {
    return this.http.get<DocumentFolder[]>(`${this.apiUrl}/folders/tree`);
  }

  // ==================== Sharing ====================

  shareWithDoctor(documentId: number, doctorId: number): Observable<Document> {
    return this.http.post<Document>(`${this.apiUrl}/${documentId}/share/${doctorId}`, {});
  }

  unshareWithDoctor(documentId: number, doctorId: number): Observable<Document> {
    return this.http.delete<Document>(`${this.apiUrl}/${documentId}/share/${doctorId}`);
  }

  toggleShareWithDoctor(documentId: number): Observable<Document> {
    return this.http.post<Document>(`${this.apiUrl}/${documentId}/toggle-share`, {});
  }

  // ==================== Statistics ====================

  getStats(): Observable<DocumentStats> {
    return this.http.get<DocumentStats>(`${this.apiUrl}/stats`);
  }

  getDocumentTypes(): Observable<DocumentType[]> {
    return this.http.get<DocumentType[]>(`${this.apiUrl}/types`);
  }
}
