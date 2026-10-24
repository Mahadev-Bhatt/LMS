import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  private readonly adminNavItems = [
    { label: 'Dashboard',          route: '/dashboard',    icon: 'home' },
    { label: 'Courses',            route: '/courses',      icon: 'book' },
    { label: 'Users',              route: '/users',        icon: 'users' },
    { label: 'Enrollments',        route: '/enrollments',  icon: 'clipboard' },
    { label: 'Learning Materials', route: '/materials',    icon: 'folder' },
    { label: 'Examinations',       route: '/examinations', icon: 'exam' },
    { label: 'Questions',          route: '/questions',    icon: 'question' },
    { label: 'Results',            route: '/results',      icon: 'chart' },
  ];

  private readonly studentNavItems = [
    { label: 'Dashboard', route: '/dashboard', icon: 'home' },
    { label: 'Courses', route: '/courses', icon: 'book' },
    { label: 'Learning Materials', route: '/my-materials', icon: 'folder' },
    { label: 'My Examinations', route: '/my-exams', icon: 'exam' },
    { label: 'My Results', route: '/my-results', icon: 'chart' },
  ];

  constructor(private auth: AuthService) {}

  get navItems() {
    const role = this.auth.user?.role;
    if (role === 'STUDENT') return this.studentNavItems;
    return role === 'INSTRUCTOR'
      ? this.adminNavItems.filter(item => item.route !== '/users')
      : this.adminNavItems;
  }
}
