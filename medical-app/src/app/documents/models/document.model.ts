export type DocumentType =
  | 'LAB_RESULT'
  | 'PRESCRIPTION'
  | 'IMAGING'
  | 'MEDICAL_REPORT'
  | 'DISCHARGE_SUMMARY'
  | 'REFERRAL'
  | 'VACCINATION'
  | 'INSURANCE'
  | 'OTHER';

export interface Document {
  id: number;
  patientId: number;
  familyMemberId?: number;
  familyMemberName?: string;
  folderId?: number;
  folderName?: string;
  documentType: DocumentType;
  title: string;
  description?: string;
  fileName: string;
  fileSize: number;
  fileSizeFormatted: string;
  mimeType: string;
  thumbnailUrl?: string;
  tags: string[];
  isSharedWithDoctor: boolean;
  sharedWithDoctors: number[];
  documentDate?: string;
  uploadedAt: string;
  updatedAt: string;
  isImage: boolean;
  isPdf: boolean;
  fileExtension: string;
}

export interface DocumentFolder {
  id: number;
  patientId: number;
  name: string;
  parentFolderId?: number;
  parentFolderName?: string;
  icon?: string;
  color?: string;
  createdAt: string;
  updatedAt: string;
  subFolders?: DocumentFolder[];
  documentCount: number;
}

export interface UploadDocumentDto {
  familyMemberId?: number;
  folderId?: number;
  documentType: DocumentType;
  title: string;
  description?: string;
  tags?: string[];
  documentDate?: string;
  shareWithDoctor?: boolean;
}

export interface DocumentStats {
  totalDocuments: number;
  totalSize: number;
  totalSizeFormatted: string;
  countByType: { [key in DocumentType]?: number };
  folderCount: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Helper function for document type display
export function getDocumentTypeInfo(type: DocumentType): { label: string; icon: string; color: string } {
  const typeInfo: { [key in DocumentType]: { label: string; icon: string; color: string } } = {
    LAB_RESULT: { label: 'Lab Results', icon: 'flask', color: '#10B981' },
    PRESCRIPTION: { label: 'Prescription', icon: 'file-medical', color: '#3B82F6' },
    IMAGING: { label: 'Imaging', icon: 'x-ray', color: '#8B5CF6' },
    MEDICAL_REPORT: { label: 'Medical Report', icon: 'file-text', color: '#F59E0B' },
    DISCHARGE_SUMMARY: { label: 'Discharge Summary', icon: 'clipboard', color: '#EF4444' },
    REFERRAL: { label: 'Referral', icon: 'share', color: '#EC4899' },
    VACCINATION: { label: 'Vaccination', icon: 'syringe', color: '#14B8A6' },
    INSURANCE: { label: 'Insurance', icon: 'shield', color: '#6366F1' },
    OTHER: { label: 'Other', icon: 'file', color: '#6B7280' }
  };
  return typeInfo[type] || typeInfo.OTHER;
}

// All document types for selection
export const DOCUMENT_TYPES: { value: DocumentType; label: string }[] = [
  { value: 'LAB_RESULT', label: 'Lab Results' },
  { value: 'PRESCRIPTION', label: 'Prescription' },
  { value: 'IMAGING', label: 'Imaging (X-Ray, CT, MRI)' },
  { value: 'MEDICAL_REPORT', label: 'Medical Report' },
  { value: 'DISCHARGE_SUMMARY', label: 'Discharge Summary' },
  { value: 'REFERRAL', label: 'Referral' },
  { value: 'VACCINATION', label: 'Vaccination Record' },
  { value: 'INSURANCE', label: 'Insurance Document' },
  { value: 'OTHER', label: 'Other' }
];
