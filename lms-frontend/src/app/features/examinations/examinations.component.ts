import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ExaminationService } from '../../core/services/examination.service';
import { CourseService } from '../../core/services/course.service';
import { ToastService } from '../../core/services/toast.service';
import { Examination, Course } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-examinations',
  templateUrl: './examinations.component.html',
  styleUrls: ['./examinations.component.css']
})
export class ExaminationsComponent implements OnInit {
  examinations: Examination[] = [];
  filtered: Examination[] = [];
  courses: Course[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  courseFilter = 'all';

  showModal = false;
  editingExam: Examination | null = null;
  saving = false;
  form!: FormGroup;

  showConfirm = false;
  deletingExam: Examination | null = null;

  constructor(
    private examService: ExaminationService,
    private courseService: CourseService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {}

  ngOnInit() { this.buildForm(); this.loadAll(); }

  buildForm(e?: Examination) {
    this.form = this.fb.group({
      title:          [e?.title ?? '',        Validators.required],
      description:    [e?.description ?? ''],
      courseId:       [e?.courseId ?? null,   Validators.required],
      durationMinutes:[e?.durationMinutes ?? 60, [Validators.required, Validators.min(1)]],
      totalMarks:     [e?.totalMarks ?? 100,  [Validators.required, Validators.min(1)]],
      passingMarks:   [e?.passingMarks ?? 50, [Validators.required, Validators.min(1)]],
      startTime:      [e?.startTime ? e.startTime.substring(0,16) : ''],
      endTime:        [e?.endTime   ? e.endTime.substring(0,16)   : ''],
      active:         [e?.active ?? true]
    });
  }

  loadAll() {
    this.loading = true; this.error = false;
    forkJoin({ exams: this.examService.getAll(), courses: this.courseService.getAll() }).subscribe({
      next: ({ exams, courses }) => {
        this.examinations = exams; this.courses = courses;
        this.applyFilter(); this.loading = false;
      },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  applyFilter() {
    let list = [...this.examinations];
    if (this.searchTerm.trim()) {
      const q = this.searchTerm.toLowerCase();
      list = list.filter(e => e.title.toLowerCase().includes(q));
    }
    if (this.courseFilter !== 'all') list = list.filter(e => e.courseId === +this.courseFilter);
    this.filtered = list;
  }

  clearFilters() { this.searchTerm = ''; this.courseFilter = 'all'; this.applyFilter(); }

  openCreate() { this.editingExam = null; this.buildForm(); this.showModal = true; }
  openEdit(e: Examination) { this.editingExam = e; this.buildForm(e); this.showModal = true; }
  closeModal() { this.showModal = false; this.saving = false; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.value;
    if (v.passingMarks > v.totalMarks) {
      this.toast.error('Passing marks cannot exceed total marks.'); return;
    }
    if (v.startTime && v.endTime && v.endTime <= v.startTime) {
      this.toast.error('End time must be after start time.'); return;
    }
    this.saving = true;
    const payload: Examination = {
      ...v,
      startTime: v.startTime || null,
      endTime:   v.endTime   || null
    };
    const req = this.editingExam
      ? this.examService.update(this.editingExam.id!, payload)
      : this.examService.create(payload);
    req.subscribe({
      next: () => { this.toast.success(this.editingExam ? 'Examination updated.' : 'Examination created.'); this.closeModal(); this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.saving = false; this.toast.error(err.error?.message ?? 'Failed to save.'); }
    });
  }

  confirmDelete(e: Examination) { this.deletingExam = e; this.showConfirm = true; }
  cancelDelete() { this.showConfirm = false; this.deletingExam = null; }
  doDelete() {
    if (!this.deletingExam?.id) return;
    this.examService.delete(this.deletingExam.id).subscribe({
      next: () => { this.toast.success('Examination deleted.'); this.showConfirm = false; this.deletingExam = null; this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.toast.error(err.error?.message ?? 'Failed to delete.'); }
    });
  }

  get f() { return this.form.controls; }
  get activeCount() { return this.examinations.filter(e => e.active).length; }
}
