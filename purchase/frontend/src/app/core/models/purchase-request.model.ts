export interface PurchaseRequestDTO {
  prNumber: number;  // PRIMARY KEY (removed prid)
  eventid: number;
  eventname: string;
  vendorid: number;
  vendorname: string;
  cdsid: string;
  requestdate: string;
  allocatedamount: number;
  prstatus: string;
  negotiationid?: number;
}

export enum PurchaseRequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}
