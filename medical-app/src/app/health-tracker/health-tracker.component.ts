import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HealthTrackerService } from './services/health-tracker.service';
import { FamilyService } from '../services/family.service';
import {
  DashboardSummary,
  HealthReading,
  HealthStats,
  HealthAlert,
  ReadingType,
  CreateHealthReading,
  getReadingTypeInfo,
  AlertSeverity
} from './models/health-reading.model';

import {
  ChartComponent,
  ApexAxisChartSeries,
  ApexChart,
  ApexXAxis,
  ApexDataLabels,
  ApexStroke,
  ApexMarkers,
  ApexYAxis,
  ApexGrid,
  ApexTitleSubtitle,
  ApexLegend,
  ApexTooltip,
  ApexFill,
  NgApexchartsModule
} from 'ng-apexcharts';

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  dataLabels: ApexDataLabels;
  stroke: ApexStroke;
  markers: ApexMarkers;
  yaxis: ApexYAxis;
  grid: ApexGrid;
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  tooltip: ApexTooltip;
  fill: ApexFill;
  colors: string[];
};

@Component({
  selector: 'app-health-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgApexchartsModule],
  templateUrl: './health-tracker.component.html'
})
export class HealthTrackerComponent implements OnInit, OnDestroy {
  @ViewChild('chart') chart!: ChartComponent;

  dashboard: DashboardSummary | null = null;
  readings: HealthReading[] = [];
  selectedType: ReadingType | null = null;
  selectedStats: HealthStats | null = null;
  isLoading = true;
  showAddModal = false;
  showHistoryModal = false;

  readingForm!: FormGroup;
  readingTypes = Object.values(ReadingType);

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;

  // Chart options
  chartOptions: Partial<ChartOptions> = {};
  chartDays = 30;

  // Alert
  showAlert = false;
  alertMsg = '';
  alertColor = 'green';

  // Family member context
  private profileSubscription?: Subscription;
  activeFamilyMemberId: number | null = null;
  activeMemberName: string | null = null;

  constructor(
    private healthService: HealthTrackerService,
    private familyService: FamilyService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initForm();

    // Subscribe to profile changes to reload data when family member changes
    this.profileSubscription = this.familyService.activeProfile$.subscribe(profile => {
      const newMemberId = profile && !profile.isOwnProfile ? profile.familyMemberId : null;
      const memberChanged = this.activeFamilyMemberId !== newMemberId;

      this.activeFamilyMemberId = newMemberId;
      this.activeMemberName = profile && !profile.isOwnProfile ? profile.name : null;

      // Reload data if member changed (or on first load)
      if (memberChanged || this.dashboard === null) {
        this.loadDashboard();
      }
    });
  }

  ngOnDestroy(): void {
    this.profileSubscription?.unsubscribe();
  }

  initForm(): void {
    this.readingForm = this.fb.group({
      readingType: ['', Validators.required],
      value: ['', [Validators.required, Validators.min(0)]],
      secondaryValue: [''],
      notes: [''],
      measuredAt: [new Date().toISOString().slice(0, 16)]
    });
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.healthService.getDashboardSummary(this.activeFamilyMemberId ?? undefined).subscribe({
      next: (summary) => {
        this.dashboard = summary;
        this.isLoading = false;

        // Auto-select first type with data
        const types = Object.keys(summary.stats) as ReadingType[];
        if (types.length > 0) {
          this.selectType(types[0]);
        }
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.isLoading = false;
      }
    });
  }

  selectType(type: ReadingType): void {
    this.selectedType = type;
    this.selectedStats = this.dashboard?.stats[type] || null;
    this.loadReadings();
    this.updateChart();
  }

  loadReadings(): void {
    if (!this.selectedType) return;

    this.healthService.getReadings(this.activeFamilyMemberId ?? undefined, this.selectedType, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.readings = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
      },
      error: (err) => console.error('Error loading readings:', err)
    });
  }

  updateChart(): void {
    if (!this.selectedType || !this.selectedStats) return;

    const chartData = this.selectedStats.chartData || [];
    const typeInfo = getReadingTypeInfo(this.selectedType);

    const categories = chartData.map(d => {
      const date = new Date(d.date);
      return date.toLocaleDateString('ro-RO', { day: '2-digit', month: 'short' });
    });

    const seriesData: ApexAxisChartSeries = [{
      name: typeInfo.nameRo,
      data: chartData.map(d => d.value)
    }];

    if (typeInfo.hasSecondary && chartData.some(d => d.secondaryValue)) {
      seriesData.push({
        name: 'Diastolic',
        data: chartData.map(d => d.secondaryValue || 0)
      });
    }

    this.chartOptions = {
      series: seriesData,
      chart: {
        height: 350,
        type: 'line',
        zoom: { enabled: true },
        toolbar: { show: true }
      },
      colors: [typeInfo.color, '#64B5F6'],
      stroke: {
        curve: 'smooth',
        width: 3
      },
      markers: {
        size: 5,
        hover: { size: 7 }
      },
      xaxis: {
        categories: categories,
        labels: {
          rotate: -45,
          style: { fontSize: '12px' }
        }
      },
      yaxis: {
        title: { text: typeInfo.unit },
        labels: {
          formatter: (val) => val.toFixed(0)
        }
      },
      grid: {
        borderColor: '#e7e7e7',
        row: {
          colors: ['#f3f3f3', 'transparent'],
          opacity: 0.5
        }
      },
      tooltip: {
        y: {
          formatter: (val) => `${val} ${typeInfo.unit}`
        }
      },
      dataLabels: { enabled: false },
      title: {
        text: `${typeInfo.nameRo} - Ultimele ${this.chartDays} zile`,
        align: 'center'
      },
      legend: {
        position: 'top',
        horizontalAlign: 'center'
      },
      fill: {
        type: 'gradient',
        gradient: {
          shade: 'dark',
          type: 'vertical',
          shadeIntensity: 0.2,
          gradientToColors: undefined,
          inverseColors: false,
          opacityFrom: 0.8,
          opacityTo: 0.2,
          stops: [0, 100]
        }
      }
    };
  }

  // ==================== Add Reading ====================

  openAddModal(type?: ReadingType): void {
    this.showAddModal = true;
    if (type) {
      this.readingForm.patchValue({ readingType: type });
    }
  }

  closeAddModal(): void {
    this.showAddModal = false;
    this.readingForm.reset();
    this.readingForm.patchValue({ measuredAt: new Date().toISOString().slice(0, 16) });
  }

  saveReading(): void {
    if (this.readingForm.invalid) return;

    const formValue = this.readingForm.value;
    const reading: CreateHealthReading = {
      familyMemberId: this.activeFamilyMemberId ?? undefined,
      readingType: formValue.readingType,
      value: parseFloat(formValue.value),
      secondaryValue: formValue.secondaryValue ? parseFloat(formValue.secondaryValue) : undefined,
      notes: formValue.notes || undefined,
      measuredAt: formValue.measuredAt ? new Date(formValue.measuredAt).toISOString() : undefined
    };

    this.healthService.createReading(reading).subscribe({
      next: (result) => {
        this.showAlertMessage('Măsurătoare salvată cu succes!', 'green');
        this.closeAddModal();
        this.loadDashboard();

        // Show alert warning if any alerts were generated
        if (result.alerts && result.alerts.length > 0) {
          const criticalAlert = result.alerts.find(a => a.severity === AlertSeverity.CRITICAL);
          if (criticalAlert) {
            setTimeout(() => {
              this.showAlertMessage(criticalAlert.message, 'red');
            }, 1500);
          }
        }
      },
      error: (err) => {
        console.error('Error saving reading:', err);
        this.showAlertMessage('Eroare la salvare', 'red');
      }
    });
  }

  // ==================== Alerts ====================

  acknowledgeAlert(alert: HealthAlert): void {
    this.healthService.acknowledgeAlert(alert.id).subscribe({
      next: () => {
        alert.isAcknowledged = true;
        this.loadDashboard();
        this.showAlertMessage('Alertă confirmată', 'green');
      },
      error: (err) => console.error('Error acknowledging alert:', err)
    });
  }

  // ==================== Delete ====================

  deleteReading(reading: HealthReading): void {
    if (!confirm('Sigur doriți să ștergeți această măsurătoare?')) return;

    this.healthService.deleteReading(reading.id).subscribe({
      next: () => {
        this.showAlertMessage('Măsurătoare ștearsă', 'green');
        this.loadDashboard();
      },
      error: (err) => {
        console.error('Error deleting reading:', err);
        this.showAlertMessage('Eroare la ștergere', 'red');
      }
    });
  }

  // ==================== Pagination ====================

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadReadings();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadReadings();
    }
  }

  // ==================== Helpers ====================

  getTypeInfo(type: ReadingType) {
    return getReadingTypeInfo(type);
  }

  getSelectedTypeInfo() {
    return this.selectedType ? getReadingTypeInfo(this.selectedType) : null;
  }

  getSeverityClass(severity: AlertSeverity): string {
    switch (severity) {
      case AlertSeverity.CRITICAL: return 'bg-red-100 text-red-800 border-red-300';
      case AlertSeverity.WARNING: return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case AlertSeverity.INFO: return 'bg-blue-100 text-blue-800 border-blue-300';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getSeverityIcon(severity: AlertSeverity): string {
    switch (severity) {
      case AlertSeverity.CRITICAL: return '🚨';
      case AlertSeverity.WARNING: return '⚠️';
      case AlertSeverity.INFO: return 'ℹ️';
      default: return '📋';
    }
  }

  getTrendIcon(trend: string): string {
    switch (trend) {
      case 'UP': return '📈';
      case 'DOWN': return '📉';
      default: return '➡️';
    }
  }

  getTrendClass(trend: string): string {
    switch (trend) {
      case 'UP': return 'text-red-500';
      case 'DOWN': return 'text-green-500';
      default: return 'text-gray-500';
    }
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString('ro-RO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  showAlertMessage(msg: string, color: string): void {
    this.alertMsg = msg;
    this.alertColor = color;
    this.showAlert = true;
    setTimeout(() => {
      this.showAlert = false;
    }, 5000);
  }

  get statsArray(): { type: ReadingType; stats: HealthStats }[] {
    if (!this.dashboard?.stats) return [];
    return Object.entries(this.dashboard.stats).map(([type, stats]) => ({
      type: type as ReadingType,
      stats: stats as HealthStats
    }));
  }

  needsSecondaryValue(): boolean {
    const type = this.readingForm.get('readingType')?.value;
    if (!type) return false;
    return getReadingTypeInfo(type as ReadingType).hasSecondary;
  }
}
