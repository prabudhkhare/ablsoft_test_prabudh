import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { InventorySummary, ProductInventory } from '../model/inventory.model';
import { InventoryService } from '../service/inventory.service';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  standalone: true,
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  displayedColumns = [
    'productSku',
    'productName',
    'category',
    'purchaseDate',
    'unitPrice',
    'quantity',
    'stockAge'
  ];

  data: ProductInventory[] = [];
  summary?: InventorySummary;

  selectedFile: File | null = null;
  uploading = false;
  loading = false;
  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;

  sortField = 'purchaseDate';
  sortDirection: 'asc' | 'desc' = 'desc';

  constructor(private service: InventoryService) { }

  ngOnInit() {
    this.load();
  }

  load() {
    this.getData();
    this.service.getSummary().subscribe(s => (this.summary = s));
  }

  getData() {
    this.loading = true;
    this.service
      .getAll(
        this.pageIndex,
        this.pageSize,
        this.sortField,
        this.sortDirection
      )
      .subscribe({
        next: (res) => {
          this.data = res.content;
          this.totalElements = res.totalElements;
        },
        error: () => {
          this.data = [];
        },
        complete: () => {
          this.loading = false;
        }
      });
  }

  onSortChange(sort: Sort) {
    let field = sort.active;
    let direction = (sort.direction || 'asc') as 'asc' | 'desc';

    if (field === 'stockAge') {
      field = 'purchaseDate';
      // Reverse direction because stockAge is derived
      direction = direction === 'asc' ? 'desc' : 'asc';
    }

    this.sortField = field;
    this.sortDirection = direction;
    this.pageIndex = 0;

    this.getData();
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.getData();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] || null;
  }

  importFile() {
    if (!this.selectedFile) return;

    this.uploading = true;

    this.service.upload(this.selectedFile).subscribe({
      next: () => {
        this.selectedFile = null;
        if (this.fileInput) {
          this.fileInput.nativeElement.value = '';
        }
        this.load();
      },
      error: () => {
        this.uploading = false;
      },
      complete: () => {
        this.uploading = false;
      }
    });
  }

  stockAge(date: string): number {
    const purchase = new Date(date);
    const today = new Date();
    return Math.floor(
      (today.getTime() - purchase.getTime()) / (1000 * 3600 * 24)
    );
  }
}

