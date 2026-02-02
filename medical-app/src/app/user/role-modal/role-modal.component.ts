import { Component, OnDestroy, OnInit } from '@angular/core';
import { ModalService } from 'src/app/services/modal.service';
import { RoleService } from 'src/app/services/role.service';

@Component({
  selector: 'app-role-modal',
  templateUrl: './role-modal.component.html'
})
export class RoleModalComponent implements OnInit, OnDestroy {

  constructor(
      public modal: ModalService,
      private roleService: RoleService
    ) {}

  ngOnInit(): void {
    this.modal.register('role')
  }
    
  ngOnDestroy() {
    this.modal.unregister('role')
  }

  selectRole(role: string) {
    // 1) Update the role service
    this.roleService.setSelectedRole(role);

    // 2) Close the role modal
    this.modal.toggleModal('role');

    // 3) Open the auth modal
    this.modal.toggleModal('auth');
  }

}
