package com.example.purchase.purchaserequest;

import java.util.Date;
import java.util.List;

public interface PurchaseRequestService {

    // Get all purchase requests
    List<PurchaseRequestDTO> getAllPurchaseRequests();

    // Get purchase request by id
    PurchaseRequestDTO getPurchaseRequestById(Integer id);

    // Get purchase requests by status
    List<PurchaseRequestDTO> getPurchaseRequestsByStatus(Status status);

    // Get purchase requests by vendor
    List<PurchaseRequestDTO> getPurchaseRequestsByVendor(Integer vendorid);

    // Get purchase requests by event
    List<PurchaseRequestDTO> getPurchaseRequestsByEvent(Integer eventid);

    // Get purchase requests by cdsid
    List<PurchaseRequestDTO> getPurchaseRequestsByCdsid(String cdsid);

    // Get purchase requests by year
    List<PurchaseRequestDTO> getPurchaseRequestsByYear(int year);

    // Get purchase requests by date range
    List<PurchaseRequestDTO> getPurchaseRequestsByDateRange(Date startDate, Date endDate);

    // Get pending purchase requests
    List<PurchaseRequestDTO> getPendingPurchaseRequests();

    // Get approved purchase requests
    List<PurchaseRequestDTO> getApprovedPurchaseRequests();

    // Create purchase request
    PurchaseRequestDTO createPurchaseRequest(PurchaseRequestDTO dto);

    // Update purchase request
    PurchaseRequestDTO updatePurchaseRequest(Integer id, PurchaseRequestDTO dto);

    // Approve purchase request
    PurchaseRequestDTO approvePurchaseRequest(Integer id);

    // Reject purchase request
    PurchaseRequestDTO rejectPurchaseRequest(Integer id);

    // Delete purchase request
    void deletePurchaseRequest(Integer id);
}
