import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { LearningMaterialService } from '../../core/services/learning-material.service';
import { CourseService } from '../../core/services/course.service';
import { ToastService } from '../../core/services/toast.service';
import { LearningMaterial, Course, MaterialType } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-materials',
  templateUrl: './materials.component.html',
  styleUrls: ['./materials.component.css']
})
export class MaterialsComponent implements OnInit {
  materials: LearningMaterial[] = [];
  filtered: LearningMaterial[] = [];
  courses: Course[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  courseFilter = 'all';

  showModal = false;
  editingMaterial: LearningMaterial | null = null;
  saving = false;
  form!: FormGroup;

  showConfirm = false;
  deletingMaterial: LearningMaterial | null = null;

  materialTypes: MaterialType[] = ['DOCUMENT', 'VIDEO', 'LINK', 'OTHER'];

  constructor(
    private materialService: LearningMaterialService,
    private courseService: CourseService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {}

  ngOnInit() { this.buildForm(); this.loadAll(); }

  buildForm(m?: LearningMaterial) {
    this.form = this.fb.group({
      title:        [m?.title ?? '',       Validators.required],
      description:  [m?.description ?? ''],
      materialType: [m?.materialType ?? 'DOCUMENT', Validators.required],
      resourceUrl:  [m?.resourceUrl ?? '', Validators.required],
      courseId:     [m?.courseId ?? null,  Validators.required]
    });
  }

  loadAll() {
    this.loading = true; this.error = false;
    forkJoin({ materials: this.materialService.getAll(), courses: this.courseService.getAll() }).subscribe({
      next: ({ materials, courses }) => {
        this.materials = materials; this.courses = courses;
        this.applyFilter(); this.loading = false;
      },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  applyFilter() {
    let list = [...this.materials];
    if (this.searchTerm.trim()) {
      const q = this.searchTerm.toLowerCase();
      list = list.filter(m => m.title.toLowerCase().includes(q));
    }
    if (this.courseFilter !== 'all') list = list.filter(m => m.courseId === +this.courseFilter);
    this.filtered = list;
  }

  clearFilters() { this.searchTerm = ''; this.courseFilter = 'all'; this.applyFilter(); }

  openCreate() { this.editingMaterial = null; this.buildForm(); this.showModal = true; }
  openEdit(m: LearningMaterial) { this.editingMaterial = m; this.buildForm(m); this.showModal = true; }
  closeModal() { this.showModal = false; this.saving = false; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const payload: LearningMaterial = { ...this.form.value };
    const req = this.editingMaterial
      ? this.materialService.update(this.editingMaterial.id!, payload)
      : this.materialService.create(payload);
    req.subscribe({
      next: () => { this.toast.success(this.editingMaterial ? 'Material updated.' : 'Material created.'); this.closeModal(); this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.saving = false; this.toast.error(err.error?.message ?? 'Failed to save.'); }
    });
  }

  confirmDelete(m: LearningMaterial) { this.deletingMaterial = m; this.showConfirm = true; }
  cancelDelete() { this.showConfirm = false; this.deletingMaterial = null; }
  doDelete() {
    if (!this.deletingMaterial?.id) return;
    this.materialService.delete(this.deletingMaterial.id).subscribe({
      next: () => { this.toast.success('Material deleted.'); this.showConfirm = false; this.deletingMaterial = null; this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.toast.error(err.error?.message ?? 'Failed to delete.'); }
    });
  }

  get f() { return this.form.controls; }
}
