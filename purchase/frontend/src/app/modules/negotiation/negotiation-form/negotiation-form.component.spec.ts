import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NegotiationFormComponent } from './negotiation-form.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';

describe('NegotiationFormComponent', () => {
  let component: NegotiationFormComponent;
  let fixture: ComponentFixture<NegotiationFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NegotiationFormComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        provideToastr()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NegotiationFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
