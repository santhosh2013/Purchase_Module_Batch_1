import { TestBed } from '@angular/core/testing';
import { NegotiationService } from './negotiation.service';
import { provideHttpClient } from '@angular/common/http';

describe('NegotiationService', () => {
  let service: NegotiationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(NegotiationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
