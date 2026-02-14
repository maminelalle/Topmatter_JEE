import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css'],
})
export class AuthComponent {
  loginForm: FormGroup;
  registerForm: FormGroup;
  loginError = '';
  registerError = '';

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onLogin(): void {
    this.loginError = '';
    if (this.loginForm.invalid) return;
    const { email, password } = this.loginForm.value;
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => (this.loginError = err.error?.message || 'Erreur de connexion'),
    });
  }

  onRegister(): void {
    this.registerError = '';
    if (this.registerForm.invalid) return;
    const { username, email, password } = this.registerForm.value;
    this.auth.register(username, email, password).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => (this.registerError = err.error?.message || "Erreur d'inscription"),
    });
  }
}
