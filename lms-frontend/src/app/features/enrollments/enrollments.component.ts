import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { UserService } from '../../core/services/user.service';
import { CourseService } from '../../core/services/course.service';
import { ToastService } from '../../core/services/toast.service';
import { Enrollment, User, Course, EnrollmentStatus } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-enrollments',
  templateUrl: './enrollments.component.html',
  styleUrls: ['./enrollments.component.css']
})
export class EnrollmentsComponent implements OnInit {
  enrollments: Enrollment[] = [];
  filtered: Enrollment[] = [];
  students: User[] = [];
  courses: Course[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  statusFilter = 'all';

  showModal = false;
  saving = false;
  form!: FormGroup;

  showStatusModal = false;
  editingEnrollment: Enrollment | null = null;
  statusForm!: FormGroup;

  showConfirm = false;
  deletingEnrollment: Enrollment | null = null;

  statusOptions: EnrollmentStatus[] = ['ENROLLED', 'COMPLETED', 'WITHDRAWN'];

  constructor(
    private enrollmentService: EnrollmentService,
    private userService: UserService,
    private courseService: CourseService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      studentId: [null, Validators.required],
      courseId:  [null, Validators.required]
    });
    this.statusForm = this.fb.group({ status: ['ENROLLED', Validators.required] });
    this.loadAll();
  }

  loadAll() {
    this.loading = true; this.error = false;
    forkJoin({
      enrollments: this.enrollmentService.getAll(),
      students: this.userService.getByType('STUDENT'),
      courses: this.courseService.getAll()
    }).subscribe({
      next: ({ enrollments, students, courses }) => {
        this.enrollments = enrollments;
        this.students = students;
        this.courses = courses;
        this.applyFilter();
        this.loading = false;
      },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  applyFilter() {
    let list = [...this.enrollments];
    if (this.searchTerm.trim()) {
      const q = this.searchTerm.toLowerCase();
      list = list.filter(e => e.studentName.toLowerCase().includes(q) || e.courseTitle.toLowerCase().includes(q));
    }
    if (this.statusFilter !== 'all') list = list.filter(e => e.status === this.statusFilter);
    this.filtered = list;
  }

  clearFilters() { this.searchTerm = ''; this.statusFilter = 'all'; this.applyFilter(); }

  openCreate() { this.form.reset({ studentId: null, courseId: null }); this.showModal = true; }
  closeModal() { this.showModal = false; this.saving = false; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.enrollmentService.enroll(this.form.value).subscribe({
      next: () => { this.toast.success('Student enrolled successfully.'); this.closeModal(); this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.saving = false; this.toast.error(err.error?.message ?? 'Enrollment failed.'); }
    });
  }

  openStatusEdit(e: Enrollment) {
    this.editingEnrollment = e;
    this.statusForm.patchValue({ status: e.status });
    this.showStatusModal = true;
  }
  closeStatusModal() { this.showStatusModal = false; }

  submitStatus() {
    if (!this.editingEnrollment?.id) return;
    this.enrollmentService.updateStatus(this.editingEnrollment.id, this.statusForm.value).subscribe({
      next: () => { this.toast.success('Status updated.'); this.closeStatusModal(); this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.toast.error(err.error?.message ?? 'Failed to update status.'); }
    });
  }

  confirmDelete(e: Enrollment) { this.deletingEnrollment = e; this.showConfirm = true; }
  cancelDelete() { this.showConfirm = false; this.deletingEnrollment = null; }
  doDelete() {
    if (!this.deletingEnrollment?.id) return;
    this.enrollmentService.delete(this.deletingEnrollment.id).subscribe({
      next: () => { this.toast.success('Enrollment deleted.'); this.showConfirm = false; this.deletingEnrollment = null; this.loadAll(); },
      error: (err: HttpErrorResponse) => { this.toast.error(err.error?.message ?? 'Failed to delete.'); }
    });
  }

  get f() { return this.form.controls; }
  get enrolledCount()   { return this.enrollments.filter(e => e.status === 'ENROLLED').length; }
  get completedCount()  { return this.enrollments.filter(e => e.status === 'COMPLETED').length; }
  get withdrawnCount()  { return this.enrollments.filter(e => e.status === 'WITHDRAWN').length; }
  get activeCourses()   { return this.courses.filter(c => c.active); }
}
