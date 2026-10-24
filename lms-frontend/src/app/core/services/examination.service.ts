import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Examination } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ExaminationService {
  private url = `${environment.apiUrl}/examinations`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Examination[]> {
    return this.http.get<Examination[]>(this.url);
  }

  getAvailableForStudent(): Observable<Examination[]> {
    return this.http.get<Examination[]>(`${this.url}/available`);
  }

  getById(id: number): Observable<Examination> {
    return this.http.get<Examination>(`${this.url}/${id}`);
  }

  getByCourse(courseId: number): Observable<Examination[]> {
    return this.http.get<Examination[]>(`${this.url}/course/${courseId}`);
  }

  create(e: Examination): Observable<Examination> {
    return this.http.post<Examination>(this.url, e);
  }

  update(id: number, e: Examination): Observable<Examination> {
    return this.http.put<Examination>(`${this.url}/${id}`, e);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
