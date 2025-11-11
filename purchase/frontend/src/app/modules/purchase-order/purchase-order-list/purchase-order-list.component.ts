import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PurchaseOrderService } from '../../../core/services/purchase-order.service';
import { PurchaseOrderDTO } from '../../../core/models/purchase-order.model';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-purchase-order-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase-order-list.component.html',
  styleUrl: './purchase-order-list.component.css'
})
export class PurchaseOrderListComponent implements OnInit {
  purchaseOrders: PurchaseOrderDTO[] = [];
  filteredOrders: PurchaseOrderDTO[] = [];
  loading = false;

  // Filter properties
  filterStatus: string = '';
  filterEventName: string = '';
  filterVendorName: string = '';

  // Sort properties
  sortOrder: 'asc' | 'desc' = 'desc'; // Default: newest first

  // Email configuration
  ldEmail: string = 'ak82@ford.com';
  vendorEmail: string = '';
  schedulerEmail: string = 'dn27@ford.com';

  //  CHANGED: Use object instead of Map for better change detection
  selectedRecipient: { [key: number]: string } = {};

  constructor(
    private purchaseOrderService: PurchaseOrderService,
    private router: Router,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.loadPurchaseOrders();
  }

  loadPurchaseOrders(): void {
    this.loading = true;
     //  FETCH ALL POs (INCLUDING NEWLY CREATED ONE)
    this.purchaseOrderService.getAllPurchaseOrders().subscribe({
      next: (data) => {
        this.purchaseOrders = data;
        this.filteredOrders = data;

        // Initialize selectedRecipient for each order
        data.forEach(order => {
          if (order.po_id && !this.selectedRecipient[order.po_id]) {
            this.selectedRecipient[order.po_id] = '';
          }
        });

        this.sortByDate();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading purchase orders:', error);
        this.toastr.error('Failed to load purchase orders', 'Error');
        this.loading = false;
      }
    });
  }


  applyFilters(): void {
    this.filteredOrders = this.purchaseOrders.filter(order => {
      const matchesStatus = !this.filterStatus || order.po_status === this.filterStatus;
      const matchesEventName = !this.filterEventName ||
        order.eventname.toLowerCase().includes(this.filterEventName.toLowerCase());
      const matchesVendorName = !this.filterVendorName ||
        order.vendorname.toLowerCase().includes(this.filterVendorName.toLowerCase());
      return matchesStatus && matchesEventName && matchesVendorName;
    });
    this.sortByDate();
  }

  clearFilters(): void {
    this.filterStatus = '';
    this.filterEventName = '';
    this.filterVendorName = '';
    this.filteredOrders = this.purchaseOrders;
    this.sortByDate();
    this.toastr.info('Filters cleared', 'Info');
  }

  sortByDate(): void {
    this.filteredOrders.sort((a, b) => {
      const dateA = new Date(a.orderdate).getTime();
      const dateB = new Date(b.orderdate).getTime();

      if (this.sortOrder === 'asc') {
        return dateA - dateB;
      } else {
        return dateB - dateA;
      }
    });
  }

  toggleSortOrder(): void {
    this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    this.sortByDate();
  }

  createNew(): void {
    this.router.navigate(['/purchase-orders/new']);
  }

  viewPurchaseOrder(id: number): void {
    this.router.navigate(['/purchase-orders/view', id]);
  }

  editPurchaseOrder(id: number): void {
    this.router.navigate(['/purchase-orders/edit', id]);
  }

  deletePurchaseOrder(id: number): void {
    if (confirm('⚠️ Are you sure you want to delete this purchase order?\n\nThis action cannot be undone.')) {
      this.toastr.info('Deleting purchase order...', 'Processing');

      this.purchaseOrderService.deletePurchaseOrder(id).subscribe({
        next: () => {
          this.toastr.success('Purchase order deleted successfully!', 'Success');
          this.loadPurchaseOrders();
        },
        error: (error) => {
          console.error('Error deleting purchase order:', error);
          this.toastr.error('Failed to delete purchase order', 'Error');
        }
      });
    }
  }

  // ===========================
  // PDF GENERATION
  // ===========================

  private createPDF(order: PurchaseOrderDTO): jsPDF {
    const doc = new jsPDF();

    // Header
    doc.setFontSize(20);
    doc.setTextColor(40, 40, 40);
    doc.text('PURCHASE ORDER', 105, 20, { align: 'center' });

    // Company Info
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text('Ford Motor Company', 105, 30, { align: 'center' });
    doc.text('Purchase Management System', 105, 35, { align: 'center' });

    // Line separator
    doc.setDrawColor(0, 123, 255);
    doc.setLineWidth(0.5);
    doc.line(20, 40, 190, 40);

    // PO Details
    doc.setFontSize(12);
    doc.setTextColor(40, 40, 40);
    doc.text(`PO Number: PO-${order.po_id}`, 20, 50);
    doc.text(`Date: ${new Date(order.orderdate).toLocaleDateString()}`, 20, 57);
    doc.text(`Status: ${order.po_status || 'PENDING'}`, 20, 64);

    doc.text(`Training Name: ${order.eventname}`, 120, 50);
    doc.text(`Vendor: ${order.vendorname}`, 120, 57);
    doc.text(`Requested by: ${order.cdsid}`, 120, 64);

    // Table with order details
    const inrAmount = order.orderamountINR || 0;
    const usdAmount = order.orderamountdollar || 0;

    autoTable(doc, {
      startY: 75,
      head: [['Description', 'Amount']],
      body: [
        ['Amount (INR)', `Rs. ${inrAmount.toFixed(2)}`],
        ['Amount (USD)', `$ ${usdAmount.toFixed(2)}`],
        ['PR Number', order.prNumber ? order.prNumber.toString() : 'N/A'],
      ],
      theme: 'grid',
      headStyles: { fillColor: [40, 167, 69] },
      margin: { left: 20, right: 20 }
    });


    // Footer
    const finalY = (doc as any).lastAutoTable.finalY || 120;
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text('This is a computer-generated document. No signature required.', 105, finalY + 20, { align: 'center' });
    doc.text(`Generated on: ${new Date().toLocaleString()}`, 105, finalY + 27, { align: 'center' });

    return doc;
  }

  viewPDF_LD(order: PurchaseOrderDTO): void {
    const doc = this.createPDF(order);
    window.open(doc.output('bloburl'), '_blank');
  }

  // ===========================
  // EMAIL FUNCTIONALITY
  // ===========================

  // CHANGED: Access object property instead of Map.get()
  getSelectedRecipient(poId: number): string {
    return this.selectedRecipient[poId] || '';
  }

  //  CHANGED: Set object property instead of Map.set()
  setSelectedRecipient(poId: number, recipient: string): void {
    this.selectedRecipient[poId] = recipient;
  }



  // Send email to selected recipient
  sendMail(order: PurchaseOrderDTO): void {
    const selectedRecipient = this.getSelectedRecipient(order.po_id!);

    if (!selectedRecipient) {
      this.toastr.warning('Please select a recipient', 'No Recipient Selected');
      return;
    }

    // Build recipient list based on selection
    const toEmails: string[] = [];

    if (selectedRecipient === 'ld') {
      toEmails.push(this.ldEmail);
    } else if (selectedRecipient === 'vendor') {
      toEmails.push(this.vendorEmail);
    } else if (selectedRecipient === 'scheduler') {
      toEmails.push(this.schedulerEmail);
    } else if (selectedRecipient === 'all') {
      toEmails.push(this.ldEmail, this.schedulerEmail);
    }

    const inrAmount = order.orderamountINR || 0;
    const usdAmount = order.orderamountdollar || 0;
    const statusText = order.po_status === 'REJECTED' ? 'REJECTED' : 'COMPLETED';

    const subject = `Purchase Order ${statusText} - PO-${order.po_id}`;
    const body = `Dear Team,

${order.po_status === 'REJECTED' ? 'URGENT: ' : ''}Purchase Order PO-${order.po_id} has been ${statusText}.

Purchase Order Details:
-----------------------
PO Number: PO-${order.po_id}
PR Number: ${order.prNumber || 'N/A'}
Training Name: ${order.eventname}
Vendor Name: ${order.vendorname}
Requested by: ${order.cdsid}
Order Date: ${new Date(order.orderdate).toLocaleDateString()}
Status: ${statusText}

Financial Details:
------------------
Amount (INR): ₹${inrAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
Amount (USD): $${usdAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}

${order.po_status === 'COMPLETED' ? 'Please download the PDF document separately and attach it to this email before sending.\n\n' : ''}${order.po_status === 'REJECTED' ? 'Please review and take necessary action.\n\n' : 'Please proceed as per the agreement.\n\n'}Best Regards,
Purchase Management System
Ford Motor Company`;

    const mailtoLink = `mailto:${toEmails.join(',')}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    window.location.href = mailtoLink;

    // CHANGED: Reset object property after sending
    this.selectedRecipient[order.po_id!] = '';
    this.toastr.success('Email client opened. Please review and send.', 'Email Ready');
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'bg-success';
      case 'PENDING': return 'bg-warning';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
