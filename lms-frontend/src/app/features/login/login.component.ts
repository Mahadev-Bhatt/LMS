import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  mode: 'login' | 'signup' = 'login';
  name = '';
  email = '';
  username = '';
  password = '';
  confirmPassword = '';
  setupCode = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {
  }

  ngOnInit(): void {
    if (this.router.url.includes('reason=unavailable')) {
      this.error = 'Cannot reach the server. Please try again when the service is available.';
    }
    this.auth.checkSession().subscribe({
      next: isLoggedIn => {
        if (isLoggedIn) this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.error = 'Cannot reach the server. Please try again when the service is available.';
      }
    });
  }

  submit(): void {
    this.error = '';
    if (this.mode === 'signup') {
      if (!this.name.trim() || !this.email.trim() || !this.password) {
        this.error = 'Please complete all required fields.';
        return;
      }
      if (this.password.length < 8 || this.password.length > 72) {
        this.error = 'Password must be between 8 and 72 characters.';
        return;
      }
      if (this.password !== this.confirmPassword) {
        this.error = 'Passwords do not match.';
        return;
      }
    } else if (!this.username.trim() || !this.password) {
      this.error = 'Please enter your username or email and password.';
      return;
    }

    this.loading = true;
    const request = this.mode === 'signup'
      ? this.auth.signup(this.name, this.email, this.password, this.setupCode.trim() || undefined)
      : this.auth.login(this.username, this.password);
    request.subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        if (error.status === 401) {
          this.error = 'Invalid username/email or password.';
        } else if (error.status === 409) {
          this.error = 'An account with that email already exists.';
        } else {
          this.error = error.error?.message ?? 'Unable to complete your request. Please try again.';
        }
      }
    });
  }

  switchMode(): void {
    this.mode = this.mode === 'login' ? 'signup' : 'login';
    this.error = '';
    this.password = '';
    this.confirmPassword = '';
    this.setupCode = '';
  }
}
