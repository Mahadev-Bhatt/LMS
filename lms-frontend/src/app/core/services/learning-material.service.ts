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

  markCompleted(id: number): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/complete`, {});
  }

  getPdfFile(resourceUrl: string): Observable<Blob> {
    const fileUrl = new URL(resourceUrl, this.url).toString();
    return this.http.get(fileUrl, { responseType: 'blob' });
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

  uploadPdf(formData: FormData, materialId?: number): Observable<LearningMaterial> {
    if (materialId !== undefined) {
      formData.append('materialId', String(materialId));
    }
    return this.http.post<LearningMaterial>(`${this.url}/upload`, formData);
  }

  update(id: number, m: LearningMaterial): Observable<LearningMaterial> {
    return this.http.put<LearningMaterial>(`${this.url}/${id}`, m);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
