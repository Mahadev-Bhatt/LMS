import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { ToastService } from '../../core/services/toast.service';
import { User, UserType } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  filtered: User[] = [];
  loading = true;
  error = false;
  searchTerm = '';
  typeFilter = 'all';

  showModal = false;
  editingUser: User | null = null;
  saving = false;
  form!: FormGroup;

  showConfirm = false;
  deletingUser: User | null = null;

  constructor(
    private userService: UserService,
    private toast: ToastService,
    private fb: FormBuilder
  ) {}

  ngOnInit() { this.buildForm(); this.load(); }

  buildForm(u?: User) {
    this.form = this.fb.group({
      name:     [u?.name ?? '',       [Validators.required, Validators.maxLength(100)]],
      email:    [u?.email ?? '',      [Validators.required, Validators.email]],
      userType: [u?.userType ?? 'STUDENT', Validators.required],
      active:   [u?.active ?? true]
    });
  }

  load() {
    this.loading = true; this.error = false;
    this.userService.getAll().subscribe({
      next: users => { this.users = users; this.applyFilter(); this.loading = false; },
      error: () => { this.error = true; this.loading = false; }
    });
  }

  applyFilter() {
    let list = [...this.users];
    if (this.searchTerm.trim()) {
      const q = this.searchTerm.toLowerCase();
      list = list.filter(u => u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q));
    }
    if (this.typeFilter !== 'all') list = list.filter(u => u.userType === this.typeFilter);
    this.filtered = list;
  }

  clearFilters() { this.searchTerm = ''; this.typeFilter = 'all'; this.applyFilter(); }

  openCreate() { this.editingUser = null; this.buildForm(); this.showModal = true; }
  openEdit(u: User) { this.editingUser = u; this.buildForm(u); this.showModal = true; }
  closeModal() { this.showModal = false; this.saving = false; }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const payload: User = { ...this.form.value };
    const req = this.editingUser
      ? this.userService.update(this.editingUser.id!, payload)
      : this.userService.create(payload);
    req.subscribe({
      next: () => { this.toast.success(this.editingUser ? 'User updated.' : 'User created.'); this.closeModal(); this.load(); },
      error: (err: HttpErrorResponse) => { this.saving = false; this.toast.error(err.error?.message ?? 'Failed to save user.'); }
    });
  }

  confirmDelete(u: User) { this.deletingUser = u; this.showConfirm = true; }
  cancelDelete() { this.showConfirm = false; this.deletingUser = null; }
  doDelete() {
    if (!this.deletingUser?.id) return;
    this.userService.delete(this.deletingUser.id).subscribe({
      next: () => { this.toast.success('User deleted.'); this.showConfirm = false; this.deletingUser = null; this.load(); },
      error: (err: HttpErrorResponse) => { this.toast.error(err.error?.message ?? 'Failed to delete user.'); }
    });
  }

  issueSetupCode(user: User): void {
    if (!user.id) return;
    this.userService.issueSetupCode(user.id).subscribe({
      next: setup => {
        window.prompt(
          `Give this one-time setup code to ${user.name}. It expires in 30 minutes:`,
          setup.code
        );
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err.error?.message ?? 'Could not create a setup code.');
      }
    });
  }

  get f() { return this.form.controls; }
  get studentCount()    { return this.users.filter(u => u.userType === 'STUDENT').length; }
  get instructorCount() { return this.users.filter(u => u.userType === 'INSTRUCTOR').length; }
}
