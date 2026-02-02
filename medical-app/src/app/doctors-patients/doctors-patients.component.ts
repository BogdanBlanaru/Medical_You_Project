import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-doctors-patients',
  templateUrl: './doctors-patients.component.html',
  styleUrls: ['./doctors-patients.component.css'],
})
export class DoctorsPatientsComponent implements OnInit {
  patients = [
    {
      id: 1,
      name: 'John Doe',
      age: 35,
      gender: 'Male',
      address: '123 Main Street, Springfield',
      lastVisit: new Date('2023-12-01'),
      condition: 'Flu',
    },
    {
      id: 2,
      name: 'Jane Smith',
      age: 29,
      gender: 'Female',
      address: '456 Elm Street, Metropolis',
      lastVisit: new Date('2023-11-15'),
      condition: 'Fatigue',
    },
    {
      id: 3,
      name: 'Alice Brown',
      age: 40,
      gender: 'Female',
      address: '789 Oak Street, Gotham',
      lastVisit: new Date('2023-10-20'),
      condition: 'Hypertension',
    },
  ];

  constructor() {}

  ngOnInit(): void {}
}
