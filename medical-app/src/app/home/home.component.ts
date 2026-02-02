import { Component, OnInit } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition,
} from '@angular/animations';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  animations: [
    trigger('expandCollapse', [
      state(
        'collapsed',
        style({
          height: '0',
          opacity: '0',
          overflow: 'hidden',
        })
      ),
      state(
        'expanded',
        style({
          height: '*',
          opacity: '1',
        })
      ),
      transition('collapsed <=> expanded', animate('500ms ease-in-out')),
    ]),
  ],
})
export class HomeComponent implements OnInit {
  faqs = [
    {
      question: 'Where can I download the app?',
      answer: 'You can download our app from the App Store or Google Play Store for free.',
    },
    {
      question: 'Do I need an account to use the app?',
      answer: 'Yes, creating an account allows you to access all features, including scheduling and tracking services.',
    },
    {
      question: 'How can I book a service?',
      answer: 'You can book a service by navigating to the Services section in the app and selecting your preferred service and time slot.',
    },
    {
      question: 'Are your services available in all locations?',
      answer: 'Our services are expanding. You can check available locations in the app or contact our support team.',
    },
    {
      question: 'Is my personal data secure?',
      answer: 'Absolutely. We use advanced encryption to ensure all your data is stored and transmitted securely.',
    },
  ];

  activeFAQ: number | null = null;

  constructor() {}

  ngOnInit(): void {}

  toggleFAQ(index: number): void {
    this.activeFAQ = this.activeFAQ === index ? null : index;
  }
}
