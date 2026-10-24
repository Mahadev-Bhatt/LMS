import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly url = `${environment.apiUrl}/auth`;
  private currentUser: AuthUser | null = null;
  private sessionChecked = false;

  constructor(private http: HttpClient, private router: Router) {}

  get user(): AuthUser | null {
    return this.currentUser;
  }

  login(username: string, password: string): Observable<AuthUser> {
    return this.withCsrfToken(() =>
      this.http.post<AuthUser>(`${this.url}/login`, { username, password })
    ).pipe(tap(user => this.setSession(user)));
  }

  signup(name: string, email: string, password: string, setupCode?: string): Observable<AuthUser> {
    return this.withCsrfToken(() =>
      this.http.post<AuthUser>(`${this.url}/signup`, { name, email, password, setupCode })
    ).pipe(tap(user => this.setSession(user)));
  }

  checkSession(): Observable<boolean> {
    if (this.sessionChecked) return of(this.currentUser !== null);
    return this.http.get<AuthUser>(`${this.url}/me`).pipe(
      tap(user => this.setSession(user)),
      map(() => true),
      catchError(error => {
        if (error.status === 401) {
          this.clearSession();
          return of(false);
        }
        return throwError(() => error);
      })
    );
  }

  logout(): Observable<void> {
    return this.withCsrfToken(() => this.http.post<void>(`${this.url}/logout`, {})).pipe(
      tap(() => {
        this.clearSession();
        this.router.navigate(['/login']);
      })
    );
  }

  private withCsrfToken<T>(request: () => Observable<T>): Observable<T> {
    return this.http.get<void>(`${this.url}/csrf`).pipe(switchMap(request));
  }

  private setSession(user: AuthUser): void {
    this.currentUser = user;
    this.sessionChecked = true;
  }

  private clearSession(): void {
    this.currentUser = null;
    this.sessionChecked = true;
  }
}
