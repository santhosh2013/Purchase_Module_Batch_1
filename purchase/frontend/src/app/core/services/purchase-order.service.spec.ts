import { TestBed } from '@angular/core/testing';
import { PurchaseOrderService } from './purchase-order.service';
import { provideHttpClient } from '@angular/common/http';

describe('PurchaseOrderService', () => {
  let service: PurchaseOrderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(PurchaseOrderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
