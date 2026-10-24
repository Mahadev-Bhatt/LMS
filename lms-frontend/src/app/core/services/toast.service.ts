import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Toast {
  id: number;
  type: 'success' | 'error' | 'info';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  toasts$ = new Subject<Toast[]>();
  private toasts: Toast[] = [];

  show(type: Toast['type'], message: string, duration = 3500) {
    const toast: Toast = { id: ++this.counter, type, message };
    this.toasts = [...this.toasts, toast];
    this.toasts$.next(this.toasts);
    setTimeout(() => this.remove(toast.id), duration);
  }

  success(msg: string) { this.show('success', msg); }
  error(msg: string)   { this.show('error', msg, 5000); }
  info(msg: string)    { this.show('info', msg); }

  remove(id: number) {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.toasts$.next(this.toasts);
  }
}
