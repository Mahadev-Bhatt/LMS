import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Question, StudentQuestion } from '../models/models';

@Injectable({ providedIn: 'root' })
export class QuestionService {
  private base = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  getByExamination(examId: number): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.base}/examinations/${examId}/questions`);
  }

  getStudentQuestions(examId: number): Observable<StudentQuestion[]> {
    return this.http.get<StudentQuestion[]>(`${this.base}/examinations/${examId}/student-questions`);
  }

  getById(id: number): Observable<Question> {
    return this.http.get<Question>(`${this.base}/questions/${id}`);
  }

  create(examId: number, q: Question): Observable<Question> {
    return this.http.post<Question>(`${this.base}/examinations/${examId}/questions`, q);
  }

  update(id: number, q: Question): Observable<Question> {
    return this.http.put<Question>(`${this.base}/questions/${id}`, q);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/questions/${id}`);
  }
}
