import { Component } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-shell',
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.css']
})
export class ShellComponent {
  pageTitle = 'Dashboard';

  private titleMap: Record<string, string> = {
    '/dashboard':   'Dashboard',
    '/courses':     'Courses',
    '/users':       'Users',
    '/enrollments': 'Enrollments',
    '/materials':   'Learning Materials',
    '/examinations':'Examinations',
    '/questions':   'Questions',
    '/results':     'Results',
    '/my-exams':    'My Examinations',
    '/my-results':  'My Results',
    '/my-materials':'My Learning Materials',
  };

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe((e: any) => {
      const base = '/' + e.urlAfterRedirects.split('/')[1].split('?')[0];
      this.pageTitle = this.titleMap[base] ?? 'LearnHub';
    });
  }
}
