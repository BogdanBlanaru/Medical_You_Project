export enum ReadingType {
  BLOOD_GLUCOSE = 'BLOOD_GLUCOSE',
  BLOOD_PRESSURE = 'BLOOD_PRESSURE',
  WEIGHT = 'WEIGHT',
  HEART_RATE = 'HEART_RATE',
  TEMPERATURE = 'TEMPERATURE',
  OXYGEN_SATURATION = 'OXYGEN_SATURATION',
  BMI = 'BMI',
  CHOLESTEROL = 'CHOLESTEROL',
  STEPS = 'STEPS'
}

export enum AlertSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}

export interface HealthAlert {
  id: number;
  severity: AlertSeverity;
  message: string;
  isAcknowledged: boolean;
  acknowledgedAt?: string;
  createdAt: string;
}

export interface HealthReading {
  id: number;
  patientId: number;
  familyMemberId?: number;
  familyMemberName?: string;
  readingType: ReadingType;
  value: number;
  secondaryValue?: number;
  unit: string;
  notes?: string;
  measuredAt: string;
  createdAt: string;
  displayValue: string;
  alerts: HealthAlert[];
  hasUnacknowledgedAlerts: boolean;
}

export interface CreateHealthReading {
  familyMemberId?: number;
  readingType: ReadingType;
  value: number;
  secondaryValue?: number;
  notes?: string;
  measuredAt?: string;
}

export interface ChartDataPoint {
  date: string;
  value: number;
  secondaryValue?: number;
}

export interface HealthStats {
  readingType: ReadingType;
  unit: string;
  latestValue?: number;
  secondaryLatestValue?: number;
  latestMeasuredAt?: string;
  averageValue?: number;
  minValue?: number;
  maxValue?: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
  trendPercentage: number;
  totalReadings: number;
  alertsCount?: number;
  chartData: ChartDataPoint[];
}

export interface DashboardSummary {
  stats: { [key: string]: HealthStats };
  totalAlerts: number;
  unacknowledgedAlerts: number;
  recentAlerts: HealthAlert[];
}

export interface ReadingTypeInfo {
  type: ReadingType;
  name: string;
  unit: string;
  nameRo: string;
  supportsManualEntry: boolean;
  hasSecondaryValue: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Helper function for reading type display
export function getReadingTypeInfo(type: ReadingType): { name: string; nameRo: string; unit: string; icon: string; color: string; hasSecondary: boolean } {
  const info: { [key: string]: { name: string; nameRo: string; unit: string; icon: string; color: string; hasSecondary: boolean } } = {
    [ReadingType.BLOOD_GLUCOSE]: { name: 'Blood Glucose', nameRo: 'Glicemie', unit: 'mg/dL', icon: 'droplet', color: '#E91E63', hasSecondary: false },
    [ReadingType.BLOOD_PRESSURE]: { name: 'Blood Pressure', nameRo: 'Tensiune', unit: 'mmHg', icon: 'heart', color: '#F44336', hasSecondary: true },
    [ReadingType.HEART_RATE]: { name: 'Heart Rate', nameRo: 'Puls', unit: 'bpm', icon: 'activity', color: '#FF5722', hasSecondary: false },
    [ReadingType.WEIGHT]: { name: 'Weight', nameRo: 'Greutate', unit: 'kg', icon: 'scale', color: '#4CAF50', hasSecondary: false },
    [ReadingType.TEMPERATURE]: { name: 'Temperature', nameRo: 'Temperatură', unit: '°C', icon: 'thermometer', color: '#FF9800', hasSecondary: false },
    [ReadingType.OXYGEN_SATURATION]: { name: 'Oxygen Saturation', nameRo: 'Saturație O₂', unit: '%', icon: 'wind', color: '#2196F3', hasSecondary: false },
    [ReadingType.BMI]: { name: 'BMI', nameRo: 'Indice corp.', unit: '', icon: 'user', color: '#9C27B0', hasSecondary: false },
    [ReadingType.CHOLESTEROL]: { name: 'Cholesterol', nameRo: 'Colesterol', unit: 'mg/dL', icon: 'droplet', color: '#795548', hasSecondary: false },
    [ReadingType.STEPS]: { name: 'Steps', nameRo: 'Pași', unit: 'steps', icon: 'walk', color: '#00BCD4', hasSecondary: false }
  };
  return info[type] || { name: type, nameRo: type, unit: '', icon: 'circle', color: '#9E9E9E', hasSecondary: false };
}
