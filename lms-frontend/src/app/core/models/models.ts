// ── Course ────────────────────────────────────────────────────────
export interface Course {
  id?: number;
  title: string;
  description: string;
  durationWeeks: number;
  active: boolean;
  createdAt?: string;
}

// ── User ──────────────────────────────────────────────────────────
export type UserType = 'STUDENT' | 'INSTRUCTOR';

export interface User {
  id?: number;
  name: string;
  email: string;
  userType: UserType;
  active: boolean;
  createdAt?: string;
  passwordSet?: boolean;
}

export interface PasswordSetupCode {
  code: string;
  expiresAt: string;
}

export interface AuthUser {
  name: string;
  email: string;
  role: string;
}

export interface StudentQuestion {
  id: number;
  questionText: string;
  marks: number;
  questionType: QuestionType;
  options: { id: number; optionText: string }[];
}

// ── Enrollment ────────────────────────────────────────────────────
export type EnrollmentStatus = 'ENROLLED' | 'COMPLETED' | 'WITHDRAWN';

export interface Enrollment {
  id?: number;
  studentId: number;
  studentName: string;
  courseId: number;
  courseTitle: string;
  enrollmentDate?: string;
  status: EnrollmentStatus;
}

export interface EnrollmentRequest {
  studentId: number;
  courseId: number;
}

export interface EnrollmentStatusRequest {
  status: EnrollmentStatus;
}

// ── Learning Material ─────────────────────────────────────────────
export type MaterialType = 'DOCUMENT' | 'VIDEO' | 'LINK' | 'OTHER';

export interface LearningMaterial {
  id?: number;
  title: string;
  description?: string;
  materialType: MaterialType;
  resourceUrl: string;
  courseId: number;
  courseTitle?: string;
  createdAt?: string;
}

// ── Examination ───────────────────────────────────────────────────
export interface Examination {
  id?: number;
  title: string;
  description?: string;
  courseId: number;
  courseTitle?: string;
  durationMinutes: number;
  totalMarks: number;
  passingMarks: number;
  startTime?: string;
  endTime?: string;
  active: boolean;
  createdAt?: string;
}

// ── Question & Option ─────────────────────────────────────────────
export type QuestionType = 'MCQ' | 'TRUE_FALSE';

export interface QuestionOption {
  id?: number;
  optionText: string;
  correct: boolean;
}

export interface Question {
  id?: number;
  questionText: string;
  marks: number;
  questionType: QuestionType;
  examinationId?: number;
  options: QuestionOption[];
}

// ── Attempt ───────────────────────────────────────────────────────
export type AttemptStatus = 'IN_PROGRESS' | 'SUBMITTED';
export type AttemptResult = 'PASS' | 'FAIL';

export interface SubmittedAnswer {
  id?: number;
  questionId: number;
  questionText: string;
  selectedOptionId?: number;
  selectedOptionText?: string;
  correctOptionText?: string;
  correct: boolean;
}

export interface ExaminationAttempt {
  id?: number;
  studentId: number;
  studentName: string;
  examinationId: number;
  examinationTitle: string;
  startedAt?: string;
  submittedAt?: string;
  obtainedMarks?: number;
  totalMarks?: number;
  passingMarks?: number;
  status: AttemptStatus;
  result?: AttemptResult;
  submittedAnswers?: SubmittedAnswer[];
}

// ── API Error ─────────────────────────────────────────────────────
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
}
