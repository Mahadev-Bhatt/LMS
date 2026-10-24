import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Enrollment, EnrollmentRequest, EnrollmentStatusRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private url = `${environment.apiUrl}/enrollments`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(this.url);
  }

  getById(id: number): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.url}/${id}`);
  }

  getByStudent(studentId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.url}/student/${studentId}`);
  }

  getByCourse(courseId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.url}/course/${courseId}`);
  }

  enroll(req: EnrollmentRequest): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.url, req);
  }

  updateStatus(id: number, req: EnrollmentStatusRequest): Observable<Enrollment> {
    return this.http.put<Enrollment>(`${this.url}/${id}/status`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
