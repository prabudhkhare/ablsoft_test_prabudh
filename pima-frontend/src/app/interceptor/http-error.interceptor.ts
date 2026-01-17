import { Injectable } from '@angular/core';
import {
    HttpEvent,
    HttpInterceptor,
    HttpHandler,
    HttpRequest,
    HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastService } from '../service/toast.service';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
    constructor(private toast: ToastService) { }

    intercept(
        req: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                if (error.status === 400 && error.error) {
                    // show backend error message in toast
                    this.toast.error(error.error);
                } else {
                    // generic message
                    this.toast.error('Something went wrong!');
                }
                return throwError(() => error);
            })
        );
    }
}
