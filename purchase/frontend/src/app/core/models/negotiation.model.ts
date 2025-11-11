export interface NegotiationDTO {
  negotiationid?: number;
  eventid: number;
  eventname: string;
  vendorid: number;
  vendorname: string;
  cdsid: string;
  negotiationdate: string;
  initialquoteamount: number;
  finalamount: number;
  negotiationstatus: string;
  notes?: string;
  prNumber?: number; // CHANGED from prid
}

export enum NegotiationStatus {
  Pending = 'Pending',
  Completed = 'Completed',
  Cancelled = 'Cancelled'
}
