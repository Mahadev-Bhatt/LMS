import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SessionInterceptor } from './core/interceptors/session.interceptor';

// Layout
import { ShellComponent }   from './layout/shell/shell.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { TopbarComponent }  from './layout/topbar/topbar.component';

// Shared
import { ToastComponent }         from './shared/components/toast/toast.component';
import { ConfirmDialogComponent } from './shared/components/confirm-dialog/confirm-dialog.component';

// Features
import { LoginComponent }        from './features/login/login.component';
import { DashboardComponent }    from './features/dashboard/dashboard.component';
import { CoursesComponent }      from './features/courses/courses.component';
import { UsersComponent }        from './features/users/users.component';
import { EnrollmentsComponent }  from './features/enrollments/enrollments.component';
import { MaterialsComponent }    from './features/materials/materials.component';
import { ExaminationsComponent } from './features/examinations/examinations.component';
import { QuestionsComponent }    from './features/questions/questions.component';
import { ResultsComponent }      from './features/results/results.component';
import { StudentExamsComponent } from './features/student-exams/student-exams.component';
import { StudentResultsComponent } from './features/student-results/student-results.component';
import { StudentMaterialsComponent } from './features/student-materials/student-materials.component';

@NgModule({
  declarations: [
    AppComponent,
    ShellComponent,
    SidebarComponent,
    TopbarComponent,
    ToastComponent,
    ConfirmDialogComponent,
    LoginComponent,
    DashboardComponent,
    CoursesComponent,
    UsersComponent,
    EnrollmentsComponent,
    MaterialsComponent,
    ExaminationsComponent,
    QuestionsComponent,
    ResultsComponent,
    StudentExamsComponent,
    StudentResultsComponent,
    StudentMaterialsComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    AppRoutingModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: SessionInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
