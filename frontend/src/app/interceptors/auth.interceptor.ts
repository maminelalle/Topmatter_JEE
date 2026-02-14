import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.auth.token;
    let req = request;
    if (token) {
      req = request.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
        withCredentials: true,
      });
    } else {
      req = request.clone({ withCredentials: true });
    }
    return next.handle(req);
  }
}
