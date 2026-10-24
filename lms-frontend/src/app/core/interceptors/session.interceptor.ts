import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable()
export class SessionInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!request.url.startsWith(environment.apiUrl)) return next.handle(request);

    let apiRequest = request.clone({ withCredentials: true });
    if (!['GET', 'HEAD', 'OPTIONS'].includes(request.method.toUpperCase())) {
      const csrfToken = this.readCookie('XSRF-TOKEN');
      if (csrfToken) {
        apiRequest = apiRequest.clone({
          setHeaders: { 'X-XSRF-TOKEN': csrfToken }
        });
      }
    }
    return next.handle(apiRequest);
  }

  private readCookie(name: string): string | null {
    const cookie = document.cookie
      .split('; ')
      .find(value => value.startsWith(`${name}=`));
    return cookie ? decodeURIComponent(cookie.substring(name.length + 1)) : null;
  }
}
