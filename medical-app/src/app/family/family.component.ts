import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FamilyService } from '../services/family.service';
import {
  FamilyGroup,
  FamilyMember,
  AddFamilyMemberRequest,
  DependentProfile,
  ActiveProfileContext,
  RelationshipType,
  RELATIONSHIP_DISPLAY,
  RELATIONSHIP_ICONS
} from '../models/family.model';
import { BloodType, BLOOD_TYPE_DISPLAY, Medication } from '../models/patient-profile.model';

@Component({
  selector: 'app-family',
  templateUrl: './family.component.html'
})
export class FamilyComponent implements OnInit {
  // Data
  familyGroup: FamilyGroup | null = null;
  members: FamilyMember[] = [];
  selectedMember: FamilyMember | null = null;
  memberProfile: DependentProfile | null = null;
  activeProfile: ActiveProfileContext | null = null;

  // State
  isLoading = true;
  isSaving = false;
  hasFamily = false;

  // Views
  activeTab: 'members' | 'profile' = 'members';

  // Modals
  showAddMemberModal = false;
  showEditMemberModal = false;
  showDeleteConfirmModal = false;
  memberToDelete: FamilyMember | null = null;

  // Forms
  addMemberForm!: FormGroup;
  editMemberForm!: FormGroup;
  profileForm!: FormGroup;

  // Options
  relationshipTypes: { value: RelationshipType; label: string }[] = [
    { value: 'CHILD', label: 'Child' },
    { value: 'SPOUSE', label: 'Spouse' },
    { value: 'PARENT', label: 'Parent' },
    { value: 'SIBLING', label: 'Sibling' },
    { value: 'OTHER', label: 'Other' }
  ];

  bloodTypes: { value: BloodType; label: string }[] = [
    { value: 'A_POSITIVE', label: 'A+' },
    { value: 'A_NEGATIVE', label: 'A-' },
    { value: 'B_POSITIVE', label: 'B+' },
    { value: 'B_NEGATIVE', label: 'B-' },
    { value: 'AB_POSITIVE', label: 'AB+' },
    { value: 'AB_NEGATIVE', label: 'AB-' },
    { value: 'O_POSITIVE', label: 'O+' },
    { value: 'O_NEGATIVE', label: 'O-' },
    { value: 'UNKNOWN', label: 'Unknown' }
  ];

  // Medical Arrays for profile
  allergies: string[] = [];
  chronicConditions: string[] = [];
  medications: Medication[] = [];
  newAllergy = '';
  newCondition = '';
  newMedication: Medication = { name: '', dosage: '', frequency: '' };

  // Alert
  showAlert = false;
  alertMsg = '';
  alertColor = 'green';

  // Constants
  relationshipDisplay = RELATIONSHIP_DISPLAY;
  relationshipIcons = RELATIONSHIP_ICONS;
  bloodTypeDisplay = BLOOD_TYPE_DISPLAY;

  constructor(
    private familyService: FamilyService,
    private fb: FormBuilder
  ) {
    this.initForms();
  }

  ngOnInit(): void {
    this.loadFamilyData();
    this.familyService.activeProfile$.subscribe(profile => {
      this.activeProfile = profile;
    });
  }

  private initForms(): void {
    this.addMemberForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      relationshipType: ['CHILD', Validators.required],
      dateOfBirth: ['']
    });

    this.editMemberForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      relationshipType: ['', Validators.required],
      dateOfBirth: ['']
    });

    this.profileForm = this.fb.group({
      phoneNumber: [''],
      gender: [''],
      address: [''],
      city: [''],
      country: [''],
      bloodType: [''],
      heightCm: ['', [Validators.min(0), Validators.max(300)]],
      weightKg: ['', [Validators.min(0), Validators.max(500)]],
      emergencyContactName: [''],
      emergencyContactPhone: [''],
      emergencyContactRelationship: ['']
    });
  }

  loadFamilyData(): void {
    this.isLoading = true;
    this.familyService.getMyFamilyGroup().subscribe({
      next: (group) => {
        if (group) {
          this.familyGroup = group;
          this.members = group.members || [];
          this.hasFamily = true;
        } else {
          // 204 No Content - user doesn't have a family group yet
          this.hasFamily = false;
          this.familyGroup = null;
          this.members = [];
        }
        this.isLoading = false;
      },
      error: (err) => {
        // No family group yet - that's OK
        if (err.status === 204 || err.status === 404) {
          this.hasFamily = false;
        } else {
          console.error('Error loading family:', err);
        }
        this.isLoading = false;
      }
    });
  }

  createFamily(): void {
    this.isSaving = true;
    this.familyService.createFamilyGroup().subscribe({
      next: (group) => {
        this.familyGroup = group;
        this.members = group.members || [];
        this.hasFamily = true;
        this.isSaving = false;
        this.showAlertMessage('Family group created successfully!', 'green');
      },
      error: (err) => {
        console.error('Error creating family:', err);
        this.showAlertMessage('Failed to create family group.', 'red');
        this.isSaving = false;
      }
    });
  }

  // ==================== Add Member ====================

  openAddMemberModal(): void {
    this.addMemberForm.reset({ relationshipType: 'CHILD' });
    this.showAddMemberModal = true;
  }

  closeAddMemberModal(): void {
    this.showAddMemberModal = false;
  }

  submitAddMember(): void {
    if (this.addMemberForm.invalid) return;

    this.isSaving = true;
    const request: AddFamilyMemberRequest = this.addMemberForm.value;

    this.familyService.addFamilyMember(request).subscribe({
      next: (member) => {
        this.members.push(member);
        this.closeAddMemberModal();
        this.isSaving = false;
        this.showAlertMessage(`${member.name} added to your family!`, 'green');
      },
      error: (err) => {
        console.error('Error adding member:', err);
        this.showAlertMessage('Failed to add family member.', 'red');
        this.isSaving = false;
      }
    });
  }

  // ==================== Edit Member ====================

  openEditMemberModal(member: FamilyMember): void {
    this.selectedMember = member;
    this.editMemberForm.patchValue({
      name: member.name,
      relationshipType: member.relationshipType,
      dateOfBirth: member.dateOfBirth || ''
    });
    this.showEditMemberModal = true;
  }

  closeEditMemberModal(): void {
    this.showEditMemberModal = false;
    this.selectedMember = null;
  }

  submitEditMember(): void {
    if (this.editMemberForm.invalid || !this.selectedMember) return;

    this.isSaving = true;
    const request: AddFamilyMemberRequest = this.editMemberForm.value;

    this.familyService.updateFamilyMember(this.selectedMember.id, request).subscribe({
      next: (member) => {
        const index = this.members.findIndex(m => m.id === member.id);
        if (index >= 0) {
          this.members[index] = member;
        }
        this.closeEditMemberModal();
        this.isSaving = false;
        this.showAlertMessage('Family member updated!', 'green');
      },
      error: (err) => {
        console.error('Error updating member:', err);
        this.showAlertMessage('Failed to update family member.', 'red');
        this.isSaving = false;
      }
    });
  }

  // ==================== Delete Member ====================

  confirmDeleteMember(member: FamilyMember): void {
    this.memberToDelete = member;
    this.showDeleteConfirmModal = true;
  }

  closeDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = false;
    this.memberToDelete = null;
  }

  deleteMember(): void {
    if (!this.memberToDelete) return;

    this.isSaving = true;
    this.familyService.removeFamilyMember(this.memberToDelete.id).subscribe({
      next: () => {
        this.members = this.members.filter(m => m.id !== this.memberToDelete!.id);
        this.closeDeleteConfirmModal();
        this.isSaving = false;
        this.showAlertMessage('Family member removed.', 'green');
      },
      error: (err) => {
        console.error('Error removing member:', err);
        this.showAlertMessage('Failed to remove family member.', 'red');
        this.isSaving = false;
      }
    });
  }

  // ==================== View Profile ====================

  viewMemberProfile(member: FamilyMember): void {
    this.selectedMember = member;
    this.activeTab = 'profile';
    this.loadMemberProfile(member.id);
  }

  loadMemberProfile(memberId: number): void {
    this.isLoading = true;
    this.familyService.getMemberProfile(memberId).subscribe({
      next: (profile) => {
        this.memberProfile = profile;
        this.populateProfileForm(profile);
        this.allergies = [...(profile.allergies || [])];
        this.chronicConditions = [...(profile.chronicConditions || [])];
        this.medications = [...(profile.medications || [])];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.showAlertMessage('Failed to load profile.', 'red');
        this.isLoading = false;
      }
    });
  }

  populateProfileForm(profile: DependentProfile): void {
    this.profileForm.patchValue({
      phoneNumber: profile.phoneNumber || '',
      gender: profile.gender || '',
      address: profile.address || '',
      city: profile.city || '',
      country: profile.country || '',
      bloodType: profile.bloodType || '',
      heightCm: profile.heightCm || '',
      weightKg: profile.weightKg || '',
      emergencyContactName: profile.emergencyContactName || '',
      emergencyContactPhone: profile.emergencyContactPhone || '',
      emergencyContactRelationship: profile.emergencyContactRelationship || ''
    });
  }

  saveProfile(): void {
    if (!this.selectedMember) return;

    this.isSaving = true;
    const updates: Partial<DependentProfile> = {
      ...this.profileForm.value,
      allergies: this.allergies,
      chronicConditions: this.chronicConditions,
      medications: this.medications
    };

    this.familyService.updateMemberProfile(this.selectedMember.id, updates).subscribe({
      next: (profile) => {
        this.memberProfile = profile;
        this.isSaving = false;
        this.showAlertMessage('Profile saved successfully!', 'green');
      },
      error: (err) => {
        console.error('Error saving profile:', err);
        this.showAlertMessage('Failed to save profile.', 'red');
        this.isSaving = false;
      }
    });
  }

  backToMembers(): void {
    this.activeTab = 'members';
    this.selectedMember = null;
    this.memberProfile = null;
  }

  // ==================== Profile Switching ====================

  switchToProfile(member: FamilyMember): void {
    this.familyService.switchProfile(member.id).subscribe({
      next: (context) => {
        this.showAlertMessage(`Switched to ${member.name}'s profile`, 'blue');
      },
      error: (err) => {
        console.error('Error switching profile:', err);
        this.showAlertMessage('Failed to switch profile.', 'red');
      }
    });
  }

  switchToOwnProfile(): void {
    this.familyService.switchToSelf().subscribe({
      next: (context) => {
        this.showAlertMessage('Switched back to your profile', 'blue');
      },
      error: (err) => {
        console.error('Error switching profile:', err);
        this.showAlertMessage('Failed to switch profile.', 'red');
      }
    });
  }

  // ==================== Medical Arrays (Allergies, Conditions, Medications) ====================

  addAllergy(): void {
    if (this.newAllergy.trim() && !this.allergies.includes(this.newAllergy.trim())) {
      this.allergies.push(this.newAllergy.trim());
      this.newAllergy = '';
    }
  }

  removeAllergy(index: number): void {
    this.allergies.splice(index, 1);
  }

  addCondition(): void {
    if (this.newCondition.trim() && !this.chronicConditions.includes(this.newCondition.trim())) {
      this.chronicConditions.push(this.newCondition.trim());
      this.newCondition = '';
    }
  }

  removeCondition(index: number): void {
    this.chronicConditions.splice(index, 1);
  }

  addMedication(): void {
    if (this.newMedication.name.trim()) {
      this.medications.push({ ...this.newMedication });
      this.newMedication = { name: '', dosage: '', frequency: '' };
    }
  }

  removeMedication(index: number): void {
    this.medications.splice(index, 1);
  }

  // ==================== Helpers ====================

  getRelationshipDisplay(type: RelationshipType): string {
    return RELATIONSHIP_DISPLAY[type] || type;
  }

  getRelationshipIcon(type: RelationshipType): string {
    return RELATIONSHIP_ICONS[type] || RELATIONSHIP_ICONS.OTHER;
  }

  getBloodTypeDisplay(type: string | undefined): string {
    if (!type) return 'Not set';
    return BLOOD_TYPE_DISPLAY[type as BloodType] || type;
  }

  getMemberInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  calculateAge(dateOfBirth: string | undefined): number | null {
    if (!dateOfBirth) return null;
    const today = new Date();
    const birth = new Date(dateOfBirth);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }

  isSelfMember(member: FamilyMember): boolean {
    return member.relationshipType === 'SELF';
  }

  private showAlertMessage(msg: string, color: string): void {
    this.alertMsg = msg;
    this.alertColor = color;
    this.showAlert = true;

    setTimeout(() => {
      this.showAlert = false;
    }, 5000);
  }
}
