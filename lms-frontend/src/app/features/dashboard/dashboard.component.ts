import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CourseService } from '../../core/services/course.service';
import { UserService } from '../../core/services/user.service';
import { EnrollmentService } from '../../core/services/enrollment.service';
import { Course, User, Enrollment } from '../../core/models/models';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  loading = true;
  error = false;

  totalCourses = 0;
  activeCourses = 0;
  totalStudents = 0;
  totalInstructors = 0;
  totalEnrollments = 0;

  recentCourses: Course[] = [];
  recentEnrollments: Enrollment[] = [];

  constructor(
    private courseService: CourseService,
    private userService: UserService,
    private enrollmentService: EnrollmentService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.isAdmin) {
      this.loading = false;
      return;
    }
    forkJoin({
      courses: this.courseService.getAll(),
      users: this.userService.getAll(),
      enrollments: this.enrollmentService.getAll()
    }).subscribe({
      next: ({ courses, users, enrollments }) => {
        this.totalCourses = courses.length;
        this.activeCourses = courses.filter(c => c.active).length;
        this.totalStudents = users.filter(u => u.userType === 'STUDENT').length;
        this.totalInstructors = users.filter(u => u.userType === 'INSTRUCTOR').length;
        this.totalEnrollments = enrollments.length;
        this.recentCourses = courses.slice(-5).reverse();
        this.recentEnrollments = enrollments.slice(-5).reverse();
        this.loading = false;
      },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  retry() { this.loading = true; this.error = false; this.ngOnInit(); }

  navigate(route: string) { this.router.navigate([route]); }

  get isStudent(): boolean { return this.auth.user?.role === 'STUDENT'; }
  get isAdmin(): boolean { return this.auth.user?.role === 'ADMIN'; }
  get studentName(): string { return this.auth.user?.name ?? 'Student'; }
}
