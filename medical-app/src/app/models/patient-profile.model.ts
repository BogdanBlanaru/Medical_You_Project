export interface PatientProfile {
  id: number;
  patientId: number;
  patientName: string;
  patientEmail: string;

  // Personal Information
  phoneNumber?: string;
  dateOfBirth?: string;
  age?: number;
  gender?: 'MALE' | 'FEMALE';
  address?: string;
  city?: string;
  country?: string;
  avatarUrl?: string;

  // Medical Information
  bloodType?: BloodType;
  heightCm?: number;
  weightKg?: number;
  bmi?: number;

  // Medical Arrays
  allergies: string[];
  chronicConditions: string[];
  currentMedications: Medication[];

  // Emergency Contact
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;

  // Medical ID
  medicalId?: string;

  // Timestamps
  createdAt?: string;
  updatedAt?: string;
}

export interface Medication {
  name: string;
  dosage?: string;
  frequency?: string;
  prescribedBy?: string;
  startDate?: string;
  notes?: string;
}

export type BloodType =
  | 'A_POSITIVE'
  | 'A_NEGATIVE'
  | 'B_POSITIVE'
  | 'B_NEGATIVE'
  | 'AB_POSITIVE'
  | 'AB_NEGATIVE'
  | 'O_POSITIVE'
  | 'O_NEGATIVE'
  | 'UNKNOWN';

export interface UpdatePatientProfile {
  phoneNumber?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE';
  address?: string;
  city?: string;
  country?: string;
  bloodType?: BloodType;
  heightCm?: number;
  weightKg?: number;
  allergies?: string[];
  chronicConditions?: string[];
  currentMedications?: Medication[];
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;
}

export interface MedicalIdCard {
  medicalId: string;
  patientName: string;
  dateOfBirth: string;
  bloodType: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  allergies: string[];
  chronicConditions: string[];
  qrCodeData: string;
}

// Blood type display names
export const BLOOD_TYPE_DISPLAY: Record<BloodType, string> = {
  A_POSITIVE: 'A+',
  A_NEGATIVE: 'A-',
  B_POSITIVE: 'B+',
  B_NEGATIVE: 'B-',
  AB_POSITIVE: 'AB+',
  AB_NEGATIVE: 'AB-',
  O_POSITIVE: 'O+',
  O_NEGATIVE: 'O-',
  UNKNOWN: 'Unknown'
};
