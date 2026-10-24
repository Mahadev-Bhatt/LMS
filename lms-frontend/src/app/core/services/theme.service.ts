import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly KEY = 'lms-theme';
  isDark$ = new BehaviorSubject<boolean>(this.loadDark());

  constructor() {
    this.apply(this.isDark$.value);
  }

  toggle() {
    const next = !this.isDark$.value;
    this.isDark$.next(next);
    localStorage.setItem(this.KEY, next ? 'dark' : 'light');
    this.apply(next);
  }

  private loadDark(): boolean {
    const saved = localStorage.getItem(this.KEY);
    if (saved) return saved === 'dark';
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  private apply(dark: boolean) {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
  }
}
