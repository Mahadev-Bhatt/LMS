import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ExaminationAttempt } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ExaminationAttemptService {
  private url = `${environment.apiUrl}/attempts`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ExaminationAttempt[]> {
    // The backend exposes attempts per student or per exam.
    // We fetch all students' attempts by getting all attempts via the base URL.
    return this.http.get<ExaminationAttempt[]>(this.url);
  }

  getById(id: number): Observable<ExaminationAttempt> {
    return this.http.get<ExaminationAttempt>(`${this.url}/${id}`);
  }

  getByStudent(studentId: number): Observable<ExaminationAttempt[]> {
    return this.http.get<ExaminationAttempt[]>(`${this.url}/student/${studentId}`);
  }

  getMine(): Observable<ExaminationAttempt[]> {
    return this.http.get<ExaminationAttempt[]>(`${this.url}/mine`);
  }

  getByExamination(examId: number): Observable<ExaminationAttempt[]> {
    return this.http.get<ExaminationAttempt[]>(`${this.url}/examination/${examId}`);
  }

  start(examinationId: number): Observable<ExaminationAttempt> {
    return this.http.post<ExaminationAttempt>(this.url, { examinationId });
  }

  submit(id: number, answers: { answers: { questionId: number; selectedOptionId: number | null }[] }): Observable<ExaminationAttempt> {
    return this.http.post<ExaminationAttempt>(`${this.url}/${id}/submit`, answers);
  }
}
