import { Component, OnDestroy, OnInit } from '@angular/core';
import { ModalService } from 'src/app/services/modal.service';

@Component({
  selector: 'app-auth-modal',
  templateUrl: './auth-modal.component.html'
})
export class AuthModalComponent implements OnInit, OnDestroy {

  constructor(public modal: ModalService) { }

  ngOnInit(): void {
    this.modal.register('auth')
  }

  ngOnDestroy() {
    this.modal.unregister('auth')
  }

}
