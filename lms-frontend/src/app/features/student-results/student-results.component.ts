import { Component, OnInit } from '@angular/core';
import { ExaminationAttempt } from '../../core/models/models';
import { ExaminationAttemptService } from '../../core/services/examination-attempt.service';

@Component({
  selector: 'app-student-results',
  templateUrl: './student-results.component.html',
  styleUrls: ['./student-results.component.css']
})
export class StudentResultsComponent implements OnInit {
  attempts: ExaminationAttempt[] = [];
  loading = true;
  error = false;
  selectedAttempt: ExaminationAttempt | null = null;

  constructor(private attemptService: ExaminationAttemptService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = false;
    this.attemptService.getMine().subscribe({
      next: attempts => { this.attempts = attempts; this.loading = false; },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  percentage(attempt: ExaminationAttempt): number {
    return attempt.obtainedMarks && attempt.totalMarks
      ? Math.round(attempt.obtainedMarks / attempt.totalMarks * 100)
      : 0;
  }

  get latestPassedAttempt(): ExaminationAttempt | null {
    return this.attempts
      .filter(attempt => attempt.status === 'SUBMITTED' && attempt.result === 'PASS')
      .sort((first, second) =>
        new Date(second.submittedAt ?? second.startedAt ?? 0).getTime()
        - new Date(first.submittedAt ?? first.startedAt ?? 0).getTime())[0] ?? null;
  }

  review(attempt: ExaminationAttempt): void {
    if (attempt.status === 'SUBMITTED') this.selectedAttempt = attempt;
  }

  closeReview(): void {
    this.selectedAttempt = null;
  }
}
