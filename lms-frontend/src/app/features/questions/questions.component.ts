import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { QuestionService } from '../../core/services/question.service';
import { ExaminationService } from '../../core/services/examination.service';
import { ToastService } from '../../core/services/toast.service';
import { Question, Examination } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-questions',
  templateUrl: './questions.component.html',
  styleUrls: ['./questions.component.css']
})
export class QuestionsComponent implements OnInit {
  examinations: Examination[] = [];
  selectedExamId: number | null = null;
  selectedExam: Examination | null = null;
  questions: Question[] = [];
  loadingExams = true;
  loadingQuestions = false;
  error = false;

  showModal = false;
  editingQuestion: Question | null = null;
  saving = false;
  form!: FormGroup;

  showConfirm = false;
  deletingQuestion: Question | null = null;

  constructor(
    private questionService: QuestionService,
    private examService: ExaminationService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.buildForm();
    this.examService.getAll().subscribe({
      next: exams => { this.examinations = exams; this.loadingExams = false; },
      error: () => { this.loadingExams = false; }
    });
  }

  buildForm(q?: Question) {
    this.form = this.fb.group({
      questionText: [q?.questionText ?? '', Validators.required],
      marks:        [q?.marks ?? 1,        [Validators.required, Validators.min(1)]],
      questionType: [q?.questionType ?? 'MCQ', Validators.required],
      options: this.fb.array(
        q?.options?.length
          ? q.options.map(o => this.fb.group({ optionText: [o.optionText, Validators.required], correct: [o.correct] }))
          : [this.newOption(), this.newOption()]
      )
    });
  }

  newOption() { return this.fb.group({ optionText: ['', Validators.required], correct: [false] }); }

  get options(): FormArray { return this.form.get('options') as FormArray; }
  get optionGroups(): FormGroup[] { return this.options.controls as FormGroup[]; }

  addOption() { this.options.push(this.newOption()); }
  removeOption(i: number) { if (this.options.length > 2) this.options.removeAt(i); }

  setCorrect(index: number) {
    this.optionGroups.forEach((g, i) => g.patchValue({ correct: i === index }));
  }

  onExamChange() {
    this.selectedExam = this.examinations.find(e => e.id === +this.selectedExamId!) ?? null;
    if (!this.selectedExamId) { this.questions = []; return; }
    this.loadingQuestions = true;
    this.questionService.getByExamination(+this.selectedExamId).subscribe({
      next: qs => { this.questions = qs; this.loadingQuestions = false; },
      error: () => { this.loadingQuestions = false; }
    });
  }

  openCreate() { this.editingQuestion = null; this.buildForm(); this.showModal = true; }
  openEdit(q: Question) { this.editingQuestion = q; this.buildForm(q); this.showModal = true; }
  closeModal() { this.showModal = false; this.saving = false; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const correctCount = this.optionGroups.filter(g => g.value.correct).length;
    if (correctCount !== 1) { this.toast.error('Exactly one correct option must be selected.'); return; }
    if (!this.selectedExamId) return;
    this.saving = true;
    const payload: Question = { ...this.form.value };
    const req = this.editingQuestion
      ? this.questionService.update(this.editingQuestion.id!, payload)
      : this.questionService.create(+this.selectedExamId, payload);
    req.subscribe({
      next: () => {
        this.toast.success(this.editingQuestion ? 'Question updated.' : 'Question added.');
        this.closeModal(); this.onExamChange();
      },
      error: (err: HttpErrorResponse) => { this.saving = false; this.toast.error(err.error?.message ?? 'Failed to save.'); }
    });
  }

  confirmDelete(q: Question) { this.deletingQuestion = q; this.showConfirm = true; }
  cancelDelete() { this.showConfirm = false; this.deletingQuestion = null; }
  doDelete() {
    if (!this.deletingQuestion?.id) return;
    this.questionService.delete(this.deletingQuestion.id).subscribe({
      next: () => { this.toast.success('Question deleted.'); this.showConfirm = false; this.deletingQuestion = null; this.onExamChange(); },
      error: (err: HttpErrorResponse) => { this.toast.error(err.error?.message ?? 'Failed to delete.'); }
    });
  }

  get f() { return this.form.controls; }
  get usedMarks() { return this.questions.reduce((s, q) => s + q.marks, 0); }
}
