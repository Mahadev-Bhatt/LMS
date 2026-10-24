import { Component, Input } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-topbar',
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.css']
})
export class TopbarComponent {
  @Input() pageTitle = '';
  logoutError = '';

  constructor(public theme: ThemeService, public auth: AuthService) {}

  get authUserName(): string {
    return this.auth.user?.name ?? 'Account';
  }

  get authUserInitial(): string {
    return this.authUserName.charAt(0).toUpperCase();
  }

  logout(): void {
    this.logoutError = '';
    this.auth.logout().subscribe({
      error: () => {
        this.logoutError = 'Unable to sign out. Please try again.';
      }
    });
  }
}
