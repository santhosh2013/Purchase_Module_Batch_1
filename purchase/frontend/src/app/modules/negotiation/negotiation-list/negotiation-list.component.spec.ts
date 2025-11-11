import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NegotiationListComponent } from './negotiation-list.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideToastr } from 'ngx-toastr';

describe('NegotiationListComponent', () => {
  let component: NegotiationListComponent;
  let fixture: ComponentFixture<NegotiationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NegotiationListComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        provideToastr()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NegotiationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
