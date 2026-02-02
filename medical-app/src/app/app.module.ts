import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UserModule } from './user/user.module';
import { NavComponent } from './nav/nav.component';
import { HomeComponent } from './home/home.component';
import { AboutComponent } from './about/about.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { AuthService } from './services/auth.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FooterComponent } from './footer/footer.component';
import { VirtualAssistantComponent } from './virtual-assistant/virtual-assistant.component';
import { IntroductionComponent } from './virtual-assistant/introduction/introduction.component';
import { RegionsComponent } from './virtual-assistant/regions/regions.component';
import { InterviewComponent } from './virtual-assistant/interview/interview.component';
import { ResultsComponent } from './virtual-assistant/results/results.component';
import { FormsModule } from '@angular/forms';
import { PatientComponent } from './virtual-assistant/patient/patient.component';
import { SymptomsComponent } from './virtual-assistant/symptoms/symptoms.component';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { VideoCallComponent } from './video-call/video-call.component';
import { PatientHistoryComponent } from './patient-history/patient-history.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { DoctorsPatientsComponent } from './doctors-patients/doctors-patients.component';
import { DoctorAppointmentsComponent } from './doctor-appointments/doctor-appointments.component';
import { PatientProfileComponent } from './patient-profile/patient-profile.component';
import { FamilyComponent } from './family/family.component';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from './shared/shared.module';
import { HealthTrackerComponent } from './health-tracker/health-tracker.component';
import { MedicationsComponent } from './medications/medications.component';
import { NgApexchartsModule } from 'ng-apexcharts';
import { DocumentsComponent } from './documents/documents.component';

@NgModule({
  declarations: [
    AppComponent,
    NavComponent,
    HomeComponent,
    AboutComponent,
    NotFoundComponent,
    FooterComponent,
    VirtualAssistantComponent,
    IntroductionComponent,
    PatientComponent,
    SymptomsComponent,
    RegionsComponent,
    InterviewComponent,
    ResultsComponent,
    VideoCallComponent,
    PatientHistoryComponent,
    DoctorsPatientsComponent,
    DoctorAppointmentsComponent,
    PatientProfileComponent,
    FamilyComponent
  ],
  imports: [
    BrowserModule,
    UserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgxChartsModule,
    SharedModule,
    NgApexchartsModule,
    HealthTrackerComponent,
    MedicationsComponent,
    DocumentsComponent
  ],
  providers: [
    AuthService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
