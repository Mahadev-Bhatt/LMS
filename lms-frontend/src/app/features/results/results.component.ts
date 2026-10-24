import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ExaminationAttemptService } from '../../core/services/examination-attempt.service';
import { ExaminationService } from '../../core/services/examination.service';
import { ExaminationAttempt, Examination } from '../../core/models/models';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {
  attempts: ExaminationAttempt[] = [];
  filtered: ExaminationAttempt[] = [];
  examinations: Examination[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  examFilter = 'all';
  resultFilter = 'all';

  selectedAttempt: ExaminationAttempt | null = null;

  constructor(
    private attemptService: ExaminationAttemptService,
    private examService: ExaminationService
  ) {}

  ngOnInit() { this.loadAll(); }

  loadAll() {
    this.loading = true; this.error = false;
    this.examService.getAll().subscribe({
      next: exams => {
        this.examinations = exams;
        if (exams.length === 0) { this.loading = false; return; }
        // Fetch attempts for every examination in parallel
        const calls = exams.map(e => this.attemptService.getByExamination(e.id!));
        forkJoin(calls).subscribe({
          next: results => {
            this.attempts = results.flat();
            this.applyFilter();
            this.loading = false;
          },
          error: () => { this.loading = false; }
        });
      },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  applyFilter() {
    let list = [...this.attempts];
    if (this.searchTerm.trim()) {
      const q = this.searchTerm.toLowerCase();
      list = list.filter(a => a.studentName.toLowerCase().includes(q) || a.examinationTitle.toLowerCase().includes(q));
    }
    if (this.examFilter !== 'all') list = list.filter(a => a.examinationId === +this.examFilter);
    if (this.resultFilter !== 'all') list = list.filter(a => a.result === this.resultFilter);
    this.filtered = list;
  }

  clearFilters() { this.searchTerm = ''; this.examFilter = 'all'; this.resultFilter = 'all'; this.applyFilter(); }

  viewDetail(a: ExaminationAttempt) { this.selectedAttempt = a; }
  closeDetail() { this.selectedAttempt = null; }

  percentage(a: ExaminationAttempt): number {
    if (!a.obtainedMarks || !a.totalMarks) return 0;
    return Math.round((a.obtainedMarks / a.totalMarks) * 100);
  }

  get passCount() { return this.attempts.filter(a => a.result === 'PASS').length; }
  get failCount() { return this.attempts.filter(a => a.result === 'FAIL').length; }
  get submittedCount() { return this.attempts.filter(a => a.status === 'SUBMITTED').length; }
}
