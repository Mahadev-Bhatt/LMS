import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ShellComponent }        from './layout/shell/shell.component';
import { DashboardComponent }    from './features/dashboard/dashboard.component';
import { CoursesComponent }      from './features/courses/courses.component';
import { UsersComponent }        from './features/users/users.component';
import { EnrollmentsComponent }  from './features/enrollments/enrollments.component';
import { MaterialsComponent }    from './features/materials/materials.component';
import { ExaminationsComponent } from './features/examinations/examinations.component';
import { QuestionsComponent }    from './features/questions/questions.component';
import { ResultsComponent }      from './features/results/results.component';
import { LoginComponent }        from './features/login/login.component';
import { AuthGuard }             from './core/guards/auth.guard';
import { RoleGuard }             from './core/guards/role.guard';
import { StudentExamsComponent } from './features/student-exams/student-exams.component';
import { StudentResultsComponent } from './features/student-results/student-results.component';
import { StudentMaterialsComponent } from './features/student-materials/student-materials.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '',             redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard',    component: DashboardComponent },
      { path: 'courses',      component: CoursesComponent },
      { path: 'users',        component: UsersComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
      { path: 'enrollments',  component: EnrollmentsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'INSTRUCTOR'] } },
      { path: 'materials',    component: MaterialsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'INSTRUCTOR'] } },
      { path: 'examinations', component: ExaminationsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'INSTRUCTOR'] } },
      { path: 'questions',    component: QuestionsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'INSTRUCTOR'] } },
      { path: 'results',      component: ResultsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN', 'INSTRUCTOR'] } },
      { path: 'my-exams',     component: StudentExamsComponent, canActivate: [RoleGuard], data: { roles: ['STUDENT'] } },
      { path: 'my-results',   component: StudentResultsComponent, canActivate: [RoleGuard], data: { roles: ['STUDENT'] } },
      { path: 'my-materials', component: StudentMaterialsComponent, canActivate: [RoleGuard], data: { roles: ['STUDENT'] } },
    ]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
