import { Component, HostListener, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { ModalService } from '../services/modal.service';
import { AuthService } from '../services/auth.service';
import { FamilyService } from '../services/family.service';
import { IUser } from '../models/user.model';
import { FamilyMember, ActiveProfileContext, RELATIONSHIP_DISPLAY, RelationshipType } from '../models/family.model';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html'
})
export class NavComponent implements OnInit {
  isMenuOpen = false;
  isProfileDropdownOpen = false;

  currentUser: IUser | null = null;
  isPacient = false;
  isDoctor = false;

  // Family/Profile switching
  familyMembers: FamilyMember[] = [];
  activeProfile: ActiveProfileContext | null = null;

  constructor(
    public modal: ModalService,
    public auth: AuthService,
    private familyService: FamilyService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Close mobile menu on route changes
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.closeMenu();
        this.closeProfileDropdown();
      }
    });

    // 1) Subscribe to user changes
    this.auth.user$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.isPacient = (user.role === 'PATIENT');
        this.isDoctor = (user.role === 'DOCTOR');

        // Load family members for patients
        if (this.isPacient) {
          this.loadFamilyMembers();
        }
      } else {
        this.isPacient = false;
        this.isDoctor = false;
        this.familyMembers = [];
        this.activeProfile = null;
      }
    });

    // Subscribe to active profile changes
    this.familyService.activeProfile$.subscribe(profile => {
      this.activeProfile = profile;
    });
  }

  loadFamilyMembers(): void {
    this.familyService.getFamilyMembers().subscribe({
      next: (members) => {
        this.familyMembers = members;
      },
      error: (err) => {
        // No family yet - that's OK
        this.familyMembers = [];
      }
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: Event): void {
    const width = (event.target as Window).innerWidth;
    if (width >= 768) {
      this.isMenuOpen = false; 
    }
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu(): void {
    this.isMenuOpen = false;
  }

  openModal($event: Event) {
    $event.preventDefault();
    this.closeMenu();
    // show the "role" modal (patient or doctor)
    this.modal.toggleModal('role');
  }

  logout(): void {
    localStorage.removeItem('users');
    this.familyService.clearActiveProfile();
    this.auth.logout();
    this.router.navigate(['/']);
  }

  // Profile Switcher Methods
  toggleProfileDropdown(): void {
    this.isProfileDropdownOpen = !this.isProfileDropdownOpen;
  }

  closeProfileDropdown(): void {
    this.isProfileDropdownOpen = false;
  }

  switchToMember(member: FamilyMember): void {
    this.familyService.switchProfile(member.id).subscribe({
      next: () => {
        this.closeProfileDropdown();
      },
      error: (err) => {
        console.error('Error switching profile:', err);
      }
    });
  }

  switchToSelf(): void {
    this.familyService.switchToSelf().subscribe({
      next: () => {
        this.closeProfileDropdown();
      },
      error: (err) => {
        console.error('Error switching to self:', err);
      }
    });
  }

  getActiveProfileName(): string {
    if (this.activeProfile && !this.activeProfile.isOwnProfile) {
      return this.activeProfile.name;
    }
    return this.currentUser?.name || 'My Profile';
  }

  getActiveProfileInitials(): string {
    const name = this.getActiveProfileName();
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  isViewingDependent(): boolean {
    return this.activeProfile !== null && !this.activeProfile.isOwnProfile;
  }

  getRelationshipDisplay(type: RelationshipType): string {
    return RELATIONSHIP_DISPLAY[type] || type;
  }

  getMemberInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  // Close dropdown when clicking outside
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-switcher')) {
      this.closeProfileDropdown();
    }
  }
}
