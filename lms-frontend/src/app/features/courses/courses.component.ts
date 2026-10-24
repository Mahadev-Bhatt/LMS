import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CourseService } from '../../core/services/course.service';
import { ToastService } from '../../core/services/toast.service';
import { Course } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-courses',
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css']
})
export class CoursesComponent implements OnInit {
  courses: Course[] = [];
  filtered: Course[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  statusFilter = 'all';

  showModal = false;
  editingCourse: Course | null = null;
  saving = false;
  form!: FormGroup;

  showConfirm = false;
  deletingCourse: Course | null = null;
  deleting = false;

  constructor(
    private courseService: CourseService,
    private toast: ToastService,
    private fb: FormBuilder,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.buildForm();
    this.load();
  }

  buildForm(course?: Course) {
    this.form = this.fb.group({
      title:         [course?.title ?? '',        [Validators.required, Validators.maxLength(150)]],
      description:   [course?.description ?? '',  [Validators.required]],
      durationWeeks: [course?.durationWeeks ?? 1, [Validators.required, Validators.min(1)]],
      active:        [course?.active ?? true]
    });
  }

  load() {
    this.loading = true; this.error = false;
    this.courseService.getAll().subscribe({
      next: courses => {
        this.courses = this.isStudent ? courses.filter(course => course.active) : courses;
        this.applyFilter();
        this.loading = false;
      },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  applyFilter() {
    let list = [...this.courses];
    if (this.searchTerm.trim()) {
      const q = this.searchTerm.toLowerCase();
      list = list.filter(c => c.title.toLowerCase().includes(q));
    }
    if (this.statusFilter === 'active')   list = list.filter(c => c.active);
    if (this.statusFilter === 'inactive') list = list.filter(c => !c.active);
    this.filtered = list;
  }

  onSearch() { this.applyFilter(); }
  onStatusFilter(val: string) { this.statusFilter = val; this.applyFilter(); }
  clearFilters() { this.searchTerm = ''; this.statusFilter = 'all'; this.applyFilter(); }

  openCreate() {
    this.editingCourse = null;
    this.buildForm();
    this.showModal = true;
  }

  openEdit(course: Course) {
    this.editingCourse = course;
    this.buildForm(course);
    this.showModal = true;
  }

  closeModal() { this.showModal = false; this.saving = false; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const payload: Course = { ...this.form.value };

    const req = this.editingCourse
      ? this.courseService.update(this.editingCourse.id!, payload)
      : this.courseService.create(payload);

    req.subscribe({
      next: () => {
        this.toast.success(this.editingCourse ? 'Course updated.' : 'Course created.');
        this.closeModal();
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.saving = false;
        this.toast.error(err.error?.message ?? 'Failed to save course.');
      }
    });
  }

  confirmDelete(course: Course) { this.deletingCourse = course; this.showConfirm = true; }
  cancelDelete() { this.showConfirm = false; this.deletingCourse = null; }

  doDelete() {
    if (!this.deletingCourse?.id) return;
    this.deleting = true;
    this.courseService.delete(this.deletingCourse.id).subscribe({
      next: () => {
        this.toast.success('Course deleted.');
        this.showConfirm = false;
        this.deletingCourse = null;
        this.deleting = false;
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.deleting = false;
        this.toast.error(err.error?.message ?? 'Failed to delete course.');
      }
    });
  }

  get f() { return this.form.controls; }
  get isStudent() { return this.auth.user?.role === 'STUDENT'; }
  get activeCount()   { return this.courses.filter(c => c.active).length; }
  get inactiveCount() { return this.courses.filter(c => !c.active).length; }
}
