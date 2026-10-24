import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LearningMaterial } from '../models/models';

@Injectable({ providedIn: 'root' })
export class LearningMaterialService {
  private url = `${environment.apiUrl}/materials`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<LearningMaterial[]> {
    return this.http.get<LearningMaterial[]>(this.url);
  }

  getMine(): Observable<LearningMaterial[]> {
    return this.http.get<LearningMaterial[]>(`${this.url}/mine`);
  }

  getByCourse(courseId: number): Observable<LearningMaterial[]> {
    return this.http.get<LearningMaterial[]>(`${this.url}/course/${courseId}`);
  }

  getById(id: number): Observable<LearningMaterial> {
    return this.http.get<LearningMaterial>(`${this.url}/${id}`);
  }

  create(m: LearningMaterial): Observable<LearningMaterial> {
    return this.http.post<LearningMaterial>(this.url, m);
  }

  update(id: number, m: LearningMaterial): Observable<LearningMaterial> {
    return this.http.put<LearningMaterial>(`${this.url}/${id}`, m);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
