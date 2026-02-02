export enum MedicationFrequency {
  ONCE_DAILY = 'ONCE_DAILY',
  TWICE_DAILY = 'TWICE_DAILY',
  THREE_TIMES_DAILY = 'THREE_TIMES_DAILY',
  FOUR_TIMES_DAILY = 'FOUR_TIMES_DAILY',
  EVERY_OTHER_DAY = 'EVERY_OTHER_DAY',
  WEEKLY = 'WEEKLY',
  AS_NEEDED = 'AS_NEEDED',
  CUSTOM = 'CUSTOM'
}

export enum MedicationStatus {
  ACTIVE = 'ACTIVE',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  DISCONTINUED = 'DISCONTINUED'
}

export enum MedicationLogStatus {
  TAKEN = 'TAKEN',
  SKIPPED = 'SKIPPED',
  MISSED = 'MISSED',
  LATE = 'LATE'
}

export interface MedicationReminder {
  id: number;
  reminderTime: string;
  isEnabled: boolean;
  label?: string;
}

export interface MedicationLog {
  id: number;
  scheduledTime?: string;
  takenAt?: string;
  status: MedicationLogStatus;
  notes?: string;
  createdAt: string;
}

export interface Medication {
  id: number;
  patientId: number;
  familyMemberId?: number;
  familyMemberName?: string;
  name: string;
  dosage?: string;
  frequency: MedicationFrequency;
  timesPerDay: number;
  specificTimes?: string[];
  instructions?: string;
  prescribedBy?: string;
  startDate: string;
  endDate?: string;
  status: MedicationStatus;
  refillReminderDays?: number;
  pillsRemaining?: number;
  pillsPerDose?: number;
  color?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  currentlyActive: boolean;
  needsRefill: boolean;
  daysRemaining: number;
  adherenceRate: number;
  reminders: MedicationReminder[];
  recentLogs: MedicationLog[];
}

export interface CreateMedication {
  familyMemberId?: number;
  name: string;
  dosage?: string;
  frequency: MedicationFrequency;
  timesPerDay?: number;
  specificTimes?: string[];
  instructions?: string;
  prescribedBy?: string;
  startDate: string;
  endDate?: string;
  refillReminderDays?: number;
  pillsRemaining?: number;
  pillsPerDose?: number;
  color?: string;
  notes?: string;
}

export interface MedicationDashboard {
  activeMedications: number;
  takenToday: number;
  remainingToday: number;
  medicationsNeedingRefill: number;
  overallAdherence: number;
  todaySchedule: Medication[];
  needRefill: Medication[];
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Helper functions
export function getFrequencyLabel(frequency: MedicationFrequency): string {
  const labels: { [key: string]: string } = {
    [MedicationFrequency.ONCE_DAILY]: 'O dată pe zi',
    [MedicationFrequency.TWICE_DAILY]: 'De două ori pe zi',
    [MedicationFrequency.THREE_TIMES_DAILY]: 'De trei ori pe zi',
    [MedicationFrequency.FOUR_TIMES_DAILY]: 'De patru ori pe zi',
    [MedicationFrequency.EVERY_OTHER_DAY]: 'O dată la două zile',
    [MedicationFrequency.WEEKLY]: 'Săptămânal',
    [MedicationFrequency.AS_NEEDED]: 'La nevoie',
    [MedicationFrequency.CUSTOM]: 'Personalizat'
  };
  return labels[frequency] || frequency;
}

export function getStatusLabel(status: MedicationStatus): string {
  const labels: { [key: string]: string } = {
    [MedicationStatus.ACTIVE]: 'Activ',
    [MedicationStatus.PAUSED]: 'Pauză',
    [MedicationStatus.COMPLETED]: 'Finalizat',
    [MedicationStatus.DISCONTINUED]: 'Întrerupt'
  };
  return labels[status] || status;
}

export function getLogStatusLabel(status: MedicationLogStatus): string {
  const labels: { [key: string]: string } = {
    [MedicationLogStatus.TAKEN]: 'Luat',
    [MedicationLogStatus.SKIPPED]: 'Sărit',
    [MedicationLogStatus.MISSED]: 'Ratat',
    [MedicationLogStatus.LATE]: 'Întârziat'
  };
  return labels[status] || status;
}

// Color palette for medications
export const MEDICATION_COLORS = [
  '#EF4444', // red
  '#F97316', // orange
  '#EAB308', // yellow
  '#22C55E', // green
  '#14B8A6', // teal
  '#3B82F6', // blue
  '#8B5CF6', // violet
  '#EC4899', // pink
  '#6B7280', // gray
];
