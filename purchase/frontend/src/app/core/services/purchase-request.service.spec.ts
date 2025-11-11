import { TestBed } from '@angular/core/testing';
import { PurchaseRequestService } from './purchase-request.service';
import { provideHttpClient } from '@angular/common/http';

describe('PurchaseRequestService', () => {
  let service: PurchaseRequestService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(PurchaseRequestService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
