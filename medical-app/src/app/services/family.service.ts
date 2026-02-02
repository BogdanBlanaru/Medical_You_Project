import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  FamilyGroup,
  FamilyMember,
  AddFamilyMemberRequest,
  DependentProfile,
  ActiveProfileContext
} from '../models/family.model';

@Injectable({
  providedIn: 'root'
})
export class FamilyService {
  private apiUrl = 'http://localhost:8080/api/family';

  // Active profile state management
  private activeProfileSubject = new BehaviorSubject<ActiveProfileContext | null>(null);
  public activeProfile$ = this.activeProfileSubject.asObservable();

  constructor(private http: HttpClient) {
    // Load active profile from localStorage on service init
    this.loadActiveProfileFromStorage();
  }

  // ==================== Family Group ====================

  /**
   * Get current user's family group
   */
  getMyFamilyGroup(): Observable<FamilyGroup> {
    return this.http.get<FamilyGroup>(this.apiUrl);
  }

  /**
   * Create a new family group
   */
  createFamilyGroup(name?: string): Observable<FamilyGroup> {
    return this.http.post<FamilyGroup>(this.apiUrl, { name });
  }

  // ==================== Family Members ====================

  /**
   * Get all family members
   */
  getFamilyMembers(): Observable<FamilyMember[]> {
    return this.http.get<FamilyMember[]>(`${this.apiUrl}/members`);
  }

  /**
   * Add a new family member
   */
  addFamilyMember(request: AddFamilyMemberRequest): Observable<FamilyMember> {
    return this.http.post<FamilyMember>(`${this.apiUrl}/members`, request);
  }

  /**
   * Get a specific family member
   */
  getFamilyMember(memberId: number): Observable<FamilyMember> {
    return this.http.get<FamilyMember>(`${this.apiUrl}/members/${memberId}`);
  }

  /**
   * Update a family member
   */
  updateFamilyMember(memberId: number, request: AddFamilyMemberRequest): Observable<FamilyMember> {
    return this.http.put<FamilyMember>(`${this.apiUrl}/members/${memberId}`, request);
  }

  /**
   * Remove a family member
   */
  removeFamilyMember(memberId: number): Observable<{ status: string; message: string }> {
    return this.http.delete<{ status: string; message: string }>(`${this.apiUrl}/members/${memberId}`);
  }

  // ==================== Member Profiles ====================

  /**
   * Get a family member's medical profile
   */
  getMemberProfile(memberId: number): Observable<DependentProfile> {
    return this.http.get<DependentProfile>(`${this.apiUrl}/members/${memberId}/profile`);
  }

  /**
   * Update a family member's medical profile
   */
  updateMemberProfile(memberId: number, profile: Partial<DependentProfile>): Observable<DependentProfile> {
    return this.http.put<DependentProfile>(`${this.apiUrl}/members/${memberId}/profile`, profile);
  }

  // ==================== Profile Switching ====================

  /**
   * Switch active profile to a family member
   */
  switchProfile(memberId: number): Observable<ActiveProfileContext> {
    return this.http.post<ActiveProfileContext>(`${this.apiUrl}/switch/${memberId}`, {}).pipe(
      tap(context => this.setActiveProfile(context))
    );
  }

  /**
   * Switch back to own profile
   */
  switchToSelf(): Observable<ActiveProfileContext> {
    return this.http.post<ActiveProfileContext>(`${this.apiUrl}/switch/self`, {}).pipe(
      tap(context => this.setActiveProfile(context))
    );
  }

  /**
   * Get current active profile context
   */
  getActiveProfileContext(memberId?: number): Observable<ActiveProfileContext> {
    const params = memberId ? `?memberId=${memberId}` : '';
    return this.http.get<ActiveProfileContext>(`${this.apiUrl}/active-profile${params}`);
  }

  // ==================== Local State Management ====================

  /**
   * Get current active profile from local state
   */
  getActiveProfile(): ActiveProfileContext | null {
    return this.activeProfileSubject.getValue();
  }

  /**
   * Set active profile in local state and localStorage
   */
  setActiveProfile(context: ActiveProfileContext): void {
    this.activeProfileSubject.next(context);
    localStorage.setItem('activeProfile', JSON.stringify(context));
  }

  /**
   * Clear active profile (reset to self)
   */
  clearActiveProfile(): void {
    this.activeProfileSubject.next(null);
    localStorage.removeItem('activeProfile');
  }

  /**
   * Check if currently viewing a dependent's profile
   */
  isViewingDependent(): boolean {
    const profile = this.getActiveProfile();
    return profile !== null && !profile.isOwnProfile;
  }

  /**
   * Get the active family member ID (null if viewing own profile)
   */
  getActiveFamilyMemberId(): number | null {
    const profile = this.getActiveProfile();
    if (profile && !profile.isOwnProfile) {
      return profile.familyMemberId;
    }
    return null;
  }

  // ==================== Private Methods ====================

  private loadActiveProfileFromStorage(): void {
    const stored = localStorage.getItem('activeProfile');
    if (stored) {
      try {
        const context = JSON.parse(stored) as ActiveProfileContext;
        this.activeProfileSubject.next(context);
      } catch (e) {
        localStorage.removeItem('activeProfile');
      }
    }
  }
}
