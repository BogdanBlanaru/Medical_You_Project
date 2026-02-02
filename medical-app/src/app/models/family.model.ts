import { Medication } from './patient-profile.model';

export type RelationshipType = 'SELF' | 'SPOUSE' | 'CHILD' | 'PARENT' | 'SIBLING' | 'OTHER';

export const RELATIONSHIP_DISPLAY: { [key in RelationshipType]: string } = {
  SELF: 'Self (You)',
  SPOUSE: 'Spouse',
  CHILD: 'Child',
  PARENT: 'Parent',
  SIBLING: 'Sibling',
  OTHER: 'Other'
};

export const RELATIONSHIP_ICONS: { [key in RelationshipType]: string } = {
  SELF: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z',
  SPOUSE: 'M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z',
  CHILD: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z',
  PARENT: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z',
  SIBLING: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z',
  OTHER: 'M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z'
};

export interface FamilyGroup {
  id: number;
  name: string;
  createdById: number;
  createdByName: string;
  members: FamilyMember[];
  createdAt: string;
}

export interface FamilyMember {
  id: number;
  familyGroupId: number;
  name: string;
  relationshipType: RelationshipType;
  dateOfBirth?: string;
  age?: number;
  isActive: boolean;
  avatarUrl?: string;
  medicalId?: string;
}

export interface AddFamilyMemberRequest {
  name: string;
  relationshipType: RelationshipType;
  dateOfBirth?: string;
}

export interface DependentProfile {
  id?: number;
  familyMemberId: number;

  // Family member info
  name: string;
  relationshipType: RelationshipType;
  dateOfBirth?: string;
  age?: number;

  // Personal Information
  phoneNumber?: string;
  gender?: 'MALE' | 'FEMALE';
  address?: string;
  city?: string;
  country?: string;
  avatarUrl?: string;

  // Medical Information
  bloodType?: string;
  heightCm?: number;
  weightKg?: number;
  bmi?: number;

  // Medical data
  allergies?: string[];
  chronicConditions?: string[];
  medications?: Medication[];

  // Emergency Contact
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  emergencyContactRelationship?: string;

  // Medical ID
  medicalId?: string;
}

export interface ActiveProfileContext {
  familyMemberId: number | null;
  name: string;
  relationshipType: RelationshipType;
  isOwnProfile: boolean;
  avatarUrl?: string;
  medicalId?: string;
}
