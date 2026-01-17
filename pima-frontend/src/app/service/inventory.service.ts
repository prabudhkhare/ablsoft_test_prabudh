import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { InventorySummary } from '../model/inventory.model';
import { ToastService } from './toast.service';

@Injectable({ providedIn: 'root' })
export class InventoryService {

    private API = 'http://localhost:8080/api/inventory';

    constructor(private http: HttpClient, private toast: ToastService) { }

    upload(file: File): Observable<void> {
        const form = new FormData();
        form.append('file', file);

        return this.http.post<void>(`${this.API}/import`, form).pipe(
            tap(() => this.toast.success('File uploaded successfully!')),
            catchError(err => {
                const msg = err?.error || 'Failed to upload file';
                this.toast.error(msg);
                return throwError(() => err);
            })
        );
    }

    getSummary(): Observable<InventorySummary> {
        return this.http.get<InventorySummary>(`${this.API}/summary`).pipe(
            catchError(err => {
                const msg = err?.error || 'Failed to load summary';
                this.toast.error(msg);
                return throwError(() => err);
            })
        );
    }

    getAll(page: number, size: number, sort: string, direction: string): Observable<any> {
        return this.http.get<any>(
            `${this.API}?page=${page}&size=${size}&sort=${sort}&direction=${direction}`
        ).pipe(
            catchError(err => {
                const msg = err?.error || 'Failed to load inventory';
                this.toast.error(msg);
                return throwError(() => err);
            })
        );
    }
}
