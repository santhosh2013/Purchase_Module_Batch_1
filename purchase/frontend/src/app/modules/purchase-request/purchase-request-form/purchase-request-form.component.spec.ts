import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PurchaseRequestFormComponent } from './purchase-request-form.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';

describe('PurchaseRequestFormComponent', () => {
  let component: PurchaseRequestFormComponent;
  let fixture: ComponentFixture<PurchaseRequestFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PurchaseRequestFormComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        provideToastr()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PurchaseRequestFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
