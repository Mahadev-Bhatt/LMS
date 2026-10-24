import { Component, OnDestroy, OnInit } from '@angular/core';
import { Examination, ExaminationAttempt, StudentQuestion } from '../../core/models/models';
import { ExaminationService } from '../../core/services/examination.service';
import { ExaminationAttemptService } from '../../core/services/examination-attempt.service';
import { QuestionService } from '../../core/services/question.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-student-exams',
  templateUrl: './student-exams.component.html',
  styleUrls: ['./student-exams.component.css']
})
export class StudentExamsComponent implements OnInit, OnDestroy {
  examinations: Examination[] = [];
  attempts: ExaminationAttempt[] = [];
  questions: StudentQuestion[] = [];
  currentExam: Examination | null = null;
  currentAttempt: ExaminationAttempt | null = null;
  answers: Record<number, number | null> = {};
  loading = true;
  submitting = false;
  error = '';
  remainingSeconds = 0;
  private timerHandle: number | null = null;

  constructor(
    private examinationService: ExaminationService,
    private attemptService: ExaminationAttemptService,
    private questionService: QuestionService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    forkJoin({
      examinations: this.examinationService.getAvailableForStudent(),
      attempts: this.attemptService.getMine()
    }).subscribe({
      next: ({ examinations, attempts }) => {
        this.examinations = examinations;
        this.attempts = attempts;
        this.loading = false;
      },
      error: () => {
        this.error = 'Could not load examinations. Please try again.';
        this.loading = false;
      }
    });
  }

  begin(exam: Examination): void {
    const existing = this.attempts.find(attempt => attempt.examinationId === exam.id);
    if (existing?.status === 'SUBMITTED') return;

    this.error = '';
    this.loading = true;
    this.questionService.getStudentQuestions(exam.id!).subscribe({
      next: questions => {
        if (questions.length === 0) {
          this.loading = false;
          this.error = 'This examination has no questions yet. Please contact your instructor.';
          return;
        }
        this.questions = questions;
        if (existing) {
          this.prepareAttempt(exam, existing);
          return;
        }
        this.attemptService.start(exam.id!).subscribe({
          next: attempt => this.prepareAttempt(exam, attempt),
          error: error => {
            this.loading = false;
            this.error = error.error?.message ?? 'Could not start this examination.';
            this.load();
          }
        });
      },
      error: error => {
        this.loading = false;
        this.error = error.error?.message ?? 'Could not load this examination.';
      }
    });
  }

  submit(): void {
    if (!this.currentAttempt || this.submitting) return;
    this.submitting = true;
    const answers = this.questions.map(question => ({
      questionId: question.id,
      selectedOptionId: this.answers[question.id] ?? null
    }));
    this.attemptService.submit(this.currentAttempt.id!, { answers }).subscribe({
      next: attempt => {
        this.stopTimer();
        this.attempts = this.attempts.map(item => item.id === attempt.id ? attempt : item);
        this.currentAttempt = null;
        this.currentExam = null;
        this.questions = [];
        this.submitting = false;
      },
      error: error => {
        this.submitting = false;
        this.error = error.error?.message ?? 'Could not submit your answers. Please try again.';
      }
    });
  }

  closeExam(): void {
    if (!window.confirm('Exit this exam? Your answers from this session will be lost.')) return;
    this.stopTimer();
    this.currentExam = null;
    this.currentAttempt = null;
    this.questions = [];
    this.answers = {};
    this.error = '';
  }

  attemptFor(exam: Examination): ExaminationAttempt | undefined {
    return this.attempts.find(attempt => attempt.examinationId === exam.id);
  }

  opensLater(exam: Examination): boolean {
    return !!exam.startTime && new Date(exam.startTime).getTime() > Date.now();
  }

  startTimeIsPast(exam: Examination): boolean {
    return !!exam.startTime && new Date(exam.startTime).getTime() <= Date.now();
  }

  formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60);
    const remainder = seconds % 60;
    return `${minutes}:${remainder.toString().padStart(2, '0')}`;
  }

  private prepareAttempt(exam: Examination, attempt: ExaminationAttempt): void {
    this.currentExam = exam;
    this.currentAttempt = attempt;
    this.answers = {};
    this.questions.forEach(question => this.answers[question.id] = null);
    this.loading = false;
    this.startTimer(exam, attempt);
  }

  private startTimer(exam: Examination, attempt: ExaminationAttempt): void {
    this.stopTimer();
    const startedAt = attempt.startedAt ? new Date(attempt.startedAt).getTime() : Date.now();
    const deadline = startedAt + exam.durationMinutes * 60_000;
    const update = () => {
      this.remainingSeconds = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
      if (this.remainingSeconds === 0 && this.currentAttempt && !this.submitting) {
        this.submit();
      }
    };
    update();
    this.timerHandle = window.setInterval(update, 1000);
  }

  private stopTimer(): void {
    if (this.timerHandle !== null) {
      window.clearInterval(this.timerHandle);
      this.timerHandle = null;
    }
  }
}
