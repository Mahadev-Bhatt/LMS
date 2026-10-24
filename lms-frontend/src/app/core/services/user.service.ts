import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PasswordSetupCode, User, UserType } from '../models/models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private url = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.url);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.url}/${id}`);
  }

  getByType(type: UserType): Observable<User[]> {
    return this.http.get<User[]>(`${this.url}/type/${type}`);
  }

  create(user: User): Observable<User> {
    return this.http.post<User>(this.url, user);
  }

  issueSetupCode(id: number): Observable<PasswordSetupCode> {
    return this.http.post<PasswordSetupCode>(`${this.url}/${id}/setup-code`, {});
  }

  update(id: number, user: User): Observable<User> {
    return this.http.put<User>(`${this.url}/${id}`, user);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
