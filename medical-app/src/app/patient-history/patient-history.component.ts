import { Component, OnInit } from '@angular/core';
import { Color, ScaleType } from '@swimlane/ngx-charts';
import { ChatLogService } from '../services/chat-log.service';
import { AppointmentService, AppointmentDto } from '../services/appointment.service';
import { forkJoin } from 'rxjs';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface PatientHistoryRecord {
  doctorName: string;
  symptoms: string[];
  results: string;
  date: Date;
  type: 'AI' | 'Appointment';
  status?: string;
}

@Component({
  selector: 'app-patient-history',
  templateUrl: './patient-history.component.html',
  styleUrls: ['./patient-history.component.css'],
})
export class PatientHistoryComponent implements OnInit {
  patientHistory: PatientHistoryRecord[] = [];
  filteredHistory: PatientHistoryRecord[] = [];
  isLoading: boolean = true;
  errorMessage: string = '';

  // Search and Filter
  searchTerm: string = '';
  filterType: 'All' | 'AI' | 'Appointment' = 'All';
  filterStatus: string = 'All';
  startDate: string = '';
  endDate: string = '';
  statusOptions: string[] = ['All', 'SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

  // Expandable rows
  expandedRows: Set<number> = new Set();

  constructor(
    private chatLogService: ChatLogService,
    private appointmentService: AppointmentService
  ) {}

  symptomsChartData: any[] = [];
  view: [number, number] = [700, 400];

  colorScheme: Color = {
    name: 'customScheme',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA'], // Define custom colors
  };

  ngOnInit(): void {
    this.loadPatientHistory();
  }

  /**
   * Load patient history from both AI consultations and appointments
   */
  loadPatientHistory(): void {
    this.isLoading = true;
    this.errorMessage = '';

    // For testing, using patient ID 1. In production, get from auth service
    const patientId = 1;

    // Fetch both AI consultation logs and appointments in parallel
    forkJoin({
      chatLogs: this.chatLogService.getAllChatLogs(),
      appointments: this.appointmentService.getAppointmentsByPatient(patientId)
    }).subscribe(
      ({ chatLogs, appointments }) => {
        console.log('✅ Chat logs loaded:', chatLogs);
        console.log('✅ Appointments loaded:', appointments);

        // Convert chat logs to history records
        const aiHistory: PatientHistoryRecord[] = chatLogs.map(log => ({
          doctorName: 'AI Virtual Assistant',
          symptoms: log.symptoms ? log.symptoms.split(', ') : [],
          results: log.prognosis || 'No diagnosis available',
          date: log.createdAt ? new Date(log.createdAt) : new Date(),
          type: 'AI' as const
        }));

        // Convert appointments to history records
        const appointmentHistory: PatientHistoryRecord[] = appointments.map(apt => ({
          doctorName: apt.doctorName || 'Unknown Doctor',
          symptoms: apt.reason ? [apt.reason] : [],
          results: apt.notes || 'Appointment scheduled',
          date: new Date(apt.appointmentDate),
          type: 'Appointment' as const,
          status: apt.status
        }));

        // Combine and sort by date (most recent first)
        this.patientHistory = [...aiHistory, ...appointmentHistory].sort(
          (a, b) => b.date.getTime() - a.date.getTime()
        );

        this.applyFilters();
        this.symptomsChartData = this.prepareSymptomsChartData();
        this.isLoading = false;
      },
      (error) => {
        console.error('❌ Error loading patient history:', error);
        this.errorMessage = 'Failed to load patient history. Please try again later.';
        this.isLoading = false;
      }
    );
  }

  private prepareSymptomsChartData(): any[] {
    const symptomCounts: { [key: string]: number } = {};

    this.patientHistory.forEach((record) => {
      record.symptoms.forEach((symptom) => {
        if (symptom && symptom.trim()) {
          symptomCounts[symptom] = (symptomCounts[symptom] || 0) + 1;
        }
      });
    });

    return Object.entries(symptomCounts).map(([name, value]) => ({
      name,
      value,
    }));
  }

  /**
   * Get CSS class for appointment status badge
   */
  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'SCHEDULED':
        return 'badge-scheduled';
      case 'CONFIRMED':
        return 'badge-confirmed';
      case 'COMPLETED':
        return 'badge-completed';
      case 'CANCELLED':
        return 'badge-cancelled';
      default:
        return 'badge-default';
    }
  }

  // ==================== SEARCH & FILTER ====================

  applyFilters(): void {
    let filtered = [...this.patientHistory];

    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(record =>
        record.doctorName.toLowerCase().includes(term) ||
        record.symptoms.some(s => s.toLowerCase().includes(term)) ||
        record.results.toLowerCase().includes(term)
      );
    }

    // Type filter
    if (this.filterType !== 'All') {
      filtered = filtered.filter(record => record.type === this.filterType);
    }

    // Status filter
    if (this.filterStatus !== 'All') {
      filtered = filtered.filter(record =>
        record.status?.toUpperCase() === this.filterStatus.toUpperCase()
      );
    }

    // Date range filter
    if (this.startDate) {
      const start = new Date(this.startDate);
      start.setHours(0, 0, 0, 0);
      filtered = filtered.filter(record => record.date >= start);
    }

    if (this.endDate) {
      const end = new Date(this.endDate);
      end.setHours(23, 59, 59, 999);
      filtered = filtered.filter(record => record.date <= end);
    }

    this.filteredHistory = filtered;
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onFilterTypeChange(type: 'All' | 'AI' | 'Appointment'): void {
    this.filterType = type;
    this.applyFilters();
  }

  onFilterStatusChange(status: string): void {
    this.filterStatus = status;
    this.applyFilters();
  }

  onDateRangeChange(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterType = 'All';
    this.filterStatus = 'All';
    this.startDate = '';
    this.endDate = '';
    this.applyFilters();
  }

  // ==================== EXPANDABLE ROWS ====================

  toggleRow(index: number): void {
    if (this.expandedRows.has(index)) {
      this.expandedRows.delete(index);
    } else {
      this.expandedRows.add(index);
    }
  }

  isRowExpanded(index: number): boolean {
    return this.expandedRows.has(index);
  }

  // ==================== PDF EXPORT ====================

  exportToPdf(): void {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();

    // Header
    doc.setFontSize(20);
    doc.setTextColor(41, 128, 185); // Light blue color
    doc.text('Patient Medical History', pageWidth / 2, 20, { align: 'center' });

    // Subtitle
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text(`Generated on ${new Date().toLocaleDateString('en-US', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    })}`, pageWidth / 2, 28, { align: 'center' });

    // Filter info
    if (this.searchTerm || this.filterType !== 'All' || this.filterStatus !== 'All' || this.startDate || this.endDate) {
      doc.setFontSize(9);
      doc.setTextColor(150, 150, 150);
      let filterText = 'Filters: ';
      if (this.searchTerm) filterText += `Search: "${this.searchTerm}" | `;
      if (this.filterType !== 'All') filterText += `Type: ${this.filterType} | `;
      if (this.filterStatus !== 'All') filterText += `Status: ${this.filterStatus} | `;
      if (this.startDate) filterText += `From: ${this.startDate} | `;
      if (this.endDate) filterText += `To: ${this.endDate}`;
      doc.text(filterText.replace(/ \| $/, ''), pageWidth / 2, 35, { align: 'center' });
    }

    // Table data
    const tableData = this.filteredHistory.map(record => [
      record.date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }),
      record.type,
      record.doctorName,
      record.symptoms.join(', ') || 'N/A',
      this.truncateText(record.results, 50),
      record.status || '-'
    ]);

    // Generate table
    autoTable(doc, {
      head: [['Date', 'Type', 'Doctor', 'Symptoms/Reason', 'Results', 'Status']],
      body: tableData,
      startY: 42,
      styles: {
        fontSize: 8,
        cellPadding: 3
      },
      headStyles: {
        fillColor: [41, 128, 185],
        textColor: 255,
        fontStyle: 'bold'
      },
      alternateRowStyles: {
        fillColor: [245, 245, 245]
      },
      columnStyles: {
        0: { cellWidth: 25 },
        1: { cellWidth: 20 },
        2: { cellWidth: 30 },
        3: { cellWidth: 40 },
        4: { cellWidth: 50 },
        5: { cellWidth: 20 }
      }
    });

    // Summary section
    const finalY = (doc as any).lastAutoTable.finalY + 10;

    doc.setFontSize(12);
    doc.setTextColor(41, 128, 185);
    doc.text('Summary', 14, finalY);

    doc.setFontSize(10);
    doc.setTextColor(50, 50, 50);
    const aiCount = this.filteredHistory.filter(r => r.type === 'AI').length;
    const appointmentCount = this.filteredHistory.filter(r => r.type === 'Appointment').length;

    doc.text(`Total Records: ${this.filteredHistory.length}`, 14, finalY + 8);
    doc.text(`AI Consultations: ${aiCount}`, 14, finalY + 15);
    doc.text(`Appointments: ${appointmentCount}`, 14, finalY + 22);

    // Footer
    const pageCount = doc.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setTextColor(150, 150, 150);
      doc.text(
        `Medical You - Page ${i} of ${pageCount}`,
        pageWidth / 2,
        doc.internal.pageSize.getHeight() - 10,
        { align: 'center' }
      );
    }

    // Save
    const filename = `patient-history-${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(filename);
  }

  private truncateText(text: string, maxLength: number): string {
    if (!text) return '';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }
}
