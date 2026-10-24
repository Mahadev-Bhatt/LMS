# LmsFrontend

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 16.2.16.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Account roles and student exams

- New accounts are created from the **Sign up** page and start as students. For an existing password-less account, an administrator issues a one-time setup code from **Users**; the user enters that code while signing up with the same email, retaining the role already assigned to that account. Codes expire after 30 minutes.
- Administrators enroll students in courses from **Enrollments**. Students can take examinations only when they have an active enrollment in the examination's active course.
- Administrators and instructors create an active examination and add its questions before students can take it.
- Students use **My Learning Materials** to open resources for actively enrolled courses, **My Examinations** to start or resume an attempt and submit answers, and **My Results** to view only their attempts and scores. Scheduled exams appear with their opening time; students cannot start them early.
- Administrative pages and student exam data are also restricted by the backend; hiding navigation items is not the only access control.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
