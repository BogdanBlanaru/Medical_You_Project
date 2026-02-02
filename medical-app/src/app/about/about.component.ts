import { Component } from '@angular/core';
import { Color, ScaleType } from '@swimlane/ngx-charts';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css'],
})
export class AboutComponent {
  // Data for Patient Satisfaction (Pie Chart)
  satisfactionData = [
    { name: 'Very Satisfied', value: 65 },
    { name: 'Satisfied', value: 25 },
    { name: 'Neutral', value: 7 },
    { name: 'Dissatisfied', value: 3 },
  ];

  // Data for Doctor Distribution (Bar Chart)
  doctorDistribution = [
    { name: 'General Practitioners', value: 60 },
    { name: 'Specialists', value: 40 },
    { name: 'Surgeons', value: 25 },
    { name: 'Pediatricians', value: 30 },
    { name: 'Dermatologists', value: 20 },
  ];

  // Data for Monthly Patient Visits (Line Chart)
  monthlyVisits = [
    { name: 'January', value: 200 },
    { name: 'February', value: 180 },
    { name: 'March', value: 220 },
    { name: 'April', value: 250 },
    { name: 'May', value: 270 },
    { name: 'June', value: 300 },
    { name: 'July', value: 320 },
    { name: 'August', value: 310 },
    { name: 'September', value: 290 },
    { name: 'October', value: 310 },
    { name: 'November', value: 280 },
    { name: 'December', value: 300 },
  ];

  // Data for App User Growth (Area Chart)
  userGrowth = [
    { name: '2020', value: 1000 },
    { name: '2021', value: 5000 },
    { name: '2022', value: 12000 },
    { name: '2023', value: 20000 },
  ];

  // Data for Ratings Breakdown (Horizontal Bar Chart)
  ratingsData = [
    { name: '5 Stars', value: 80 },
    { name: '4 Stars', value: 50 },
    { name: '3 Stars', value: 30 },
    { name: '2 Stars', value: 15 },
    { name: '1 Star', value: 5 },
  ];

  // Updated color scheme with required properties
  colorScheme: Color = {
    name: 'customScheme',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA', '#1F77B4'],
  };

  view: [number, number] = [700, 400]; // Chart view dimensions

  constructor() {}
}
