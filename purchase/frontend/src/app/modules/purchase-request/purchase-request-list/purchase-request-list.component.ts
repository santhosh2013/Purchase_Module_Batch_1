import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PurchaseRequestService } from '../../../core/services/purchase-request.service';
import { PurchaseRequestDTO } from '../../../core/models/purchase-request.model';
import { NegotiationService } from '../../../core/services/negotiation.service';

@Component({
  selector: 'app-purchase-request-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase-request-list.component.html',
  styleUrl: './purchase-request-list.component.css'
})
export class PurchaseRequestListComponent implements OnInit {
  purchaseRequests: PurchaseRequestDTO[] = [];
  filteredRequests: PurchaseRequestDTO[] = [];
  loading = false;

  // Filter properties
  filterStatus: string = '';
  filterEventName: string = '';
  filterVendorName: string = '';

  // Sort properties
  sortOrder: 'asc' | 'desc' = 'desc'; // Default: newest first

  constructor(
    private purchaseRequestService: PurchaseRequestService,
    private negotiationService: NegotiationService,
    private router: Router,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    // AUTOMATICALLY LOADS DATA ON PAGE LOAD
    this.loadPurchaseRequests();
  }

  loadPurchaseRequests(): void {
    this.loading = true;
    //  CALL BACKEND API
    this.purchaseRequestService.getAllPurchaseRequests().subscribe({
      next: (data) => {
        //  STORE DATA IN ARRAY
        this.purchaseRequests = data;
        this.filteredRequests = data;
        this.sortByDate(); // Apply default sort
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading purchase requests:', error);
        this.toastr.error('Failed to load purchase requests', 'Error');
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredRequests = this.purchaseRequests.filter(request => {
      const matchesStatus = !this.filterStatus || request.prstatus === this.filterStatus;
      const matchesEventName = !this.filterEventName || 
        request.eventname.toLowerCase().includes(this.filterEventName.toLowerCase());
      const matchesVendorName = !this.filterVendorName || 
        request.vendorname.toLowerCase().includes(this.filterVendorName.toLowerCase());
      return matchesStatus && matchesEventName && matchesVendorName;
    });
    this.sortByDate(); // Re-apply sort after filtering
  }

  clearFilters(): void {
    this.filterStatus = '';
    this.filterEventName = '';
    this.filterVendorName = '';
    this.filteredRequests = this.purchaseRequests;
    this.sortByDate(); // Re-apply sort
    this.toastr.info('Filters cleared', 'Info');
  }

  sortByDate(): void {
    this.filteredRequests.sort((a, b) => {
      const dateA = new Date(a.requestdate).getTime();
      const dateB = new Date(b.requestdate).getTime();

      if (this.sortOrder === 'asc') {
        return dateA - dateB; // Oldest first
      } else {
        return dateB - dateA; // Newest first
      }
    });
  }

  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.sortByDate();
  }

  createNew(): void {
    this.router.navigate(['/purchase-requests/new']);
  }

  viewPurchaseRequest(prNumber: number): void {
    this.router.navigate(['/purchase-requests/view', prNumber]);
  }

  editPurchaseRequest(prNumber: number): void {
    this.router.navigate(['/purchase-requests/edit', prNumber]);
  }

  deletePurchaseRequest(prNumber: number): void {
    if (confirm('⚠️ Are you sure you want to delete this purchase request?\n\nThis action cannot be undone.')) {
      this.toastr.info('Deleting purchase request...', 'Processing');

      this.purchaseRequestService.deletePurchaseRequest(prNumber).subscribe({
        next: () => {
          this.toastr.success('Purchase request deleted successfully!', 'Success');
          this.loadPurchaseRequests();
        },
        error: (error) => {
          console.error('Error deleting purchase request:', error);
          this.toastr.error('Failed to delete purchase request', 'Error');
        }
      });
    }
  }

  // Handle negotiate button click
  handleNegotiate(request: PurchaseRequestDTO): void {
    // Check if negotiation already exists
    if (request.negotiationid) {
      // Navigate to existing negotiation
      this.toastr.info('Opening existing negotiation...', 'Negotiation Found');
      this.router.navigate(['/negotiations/edit', request.negotiationid]);
    } else {
      // Create new negotiation
      this.createNegotiation(request.prNumber!);
    }
  }

  // Create new negotiation
  createNegotiation(prNumber: number): void {
    this.toastr.info('Creating negotiation...', 'Processing');
     // CALL BACKEND API
    this.negotiationService.createNegotiationFromPR(prNumber).subscribe({
      next: (negotiation) => {
        this.toastr.success('Negotiation created successfully!', 'Success');
        // NAVIGATE TO NEGOTIATION FORM WITH DATA
        this.router.navigate(['/negotiations/edit', negotiation.negotiationid]);
      },
      error: (error) => {
        console.error('Error creating negotiation:', error);
        
        // CHECK IF NEGOTIATION ALREADY EXISTS
        if (error.status === 400 && error.error && error.error.includes('already exists')) {
          // Reload PR to get the negotiation ID
          this.purchaseRequestService.getPurchaseRequestById(prNumber).subscribe({
            next: (pr) => {
              if (pr.negotiationid) {
                this.toastr.info('Navigating to existing negotiation...', 'Already Exists');
                this.router.navigate(['/negotiations/edit', pr.negotiationid]);
              } else {
                this.toastr.error('Failed to create negotiation', 'Error');
              }
            },
            error: () => {
              this.toastr.error('Failed to create negotiation', 'Error');
            }
          });
        } else {
          this.toastr.error('Failed to create negotiation', 'Error');
        }
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'PENDING': return 'bg-warning';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
