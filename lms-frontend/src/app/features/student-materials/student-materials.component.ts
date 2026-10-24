import { Component, OnInit } from '@angular/core';
import { LearningMaterialService } from '../../core/services/learning-material.service';
import { LearningMaterial } from '../../core/models/models';

@Component({
  selector: 'app-student-materials',
  templateUrl: './student-materials.component.html',
  styleUrls: ['./student-materials.component.css']
})
export class StudentMaterialsComponent implements OnInit {
  materials: LearningMaterial[] = [];
  filtered: LearningMaterial[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  courseFilter = 'all';

  constructor(private materialService: LearningMaterialService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = false;
    this.materialService.getMine().subscribe({
      next: materials => {
        this.materials = materials;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    const query = this.searchTerm.trim().toLowerCase();
    this.filtered = this.materials.filter(material =>
      (!query || material.title.toLowerCase().includes(query)
        || (material.description ?? '').toLowerCase().includes(query))
      && (this.courseFilter === 'all' || material.courseId === Number(this.courseFilter)));
  }

  get courses(): { id: number; title: string }[] {
    return [...new Map(this.materials.map(material =>
      [material.courseId, { id: material.courseId, title: material.courseTitle ?? 'Course' }])).values()];
  }
}
