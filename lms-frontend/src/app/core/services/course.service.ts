import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Course } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private url = `${environment.apiUrl}/courses`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Course[]> {
    return this.http.get<Course[]>(this.url);
  }

  getById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.url}/${id}`);
  }

  search(title: string): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.url}/search`, { params: new HttpParams().set('title', title) });
  }

  create(course: Course): Observable<Course> {
    return this.http.post<Course>(this.url, course);
  }

  update(id: number, course: Course): Observable<Course> {
    return this.http.put<Course>(`${this.url}/${id}`, course);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
