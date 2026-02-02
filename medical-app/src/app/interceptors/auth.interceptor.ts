import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // 1) Retrieve the token from localStorage
    const token = localStorage.getItem('token');

    console.log('[AuthInterceptor] Request URL:', req.url);
    console.log('[AuthInterceptor] Token exists:', !!token);

    // 2) If the token exists, clone the request and add the Authorization header
    if (token) {
      const clonedRequest = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('[AuthInterceptor] Added Authorization header');
      // 3) Pass the cloned request instead of the original request to next.handle
      return next.handle(clonedRequest);
    }

    console.log('[AuthInterceptor] No token, passing request as-is');
    // If no token, just pass the request as-is
    return next.handle(req);
  }
}
