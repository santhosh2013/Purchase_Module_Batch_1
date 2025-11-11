package com.example.purchase.purchaseorder;

import java.util.Date;
import java.util.List;

public interface PurchaseOrderService {

    // Get all purchase orders
    List<PurchaseOrderDTO> getAllPurchaseOrders();

    // Get purchase order by id
    PurchaseOrderDTO getPurchaseOrderById(Integer id);

    // Get purchase orders by status
    List<PurchaseOrderDTO> getPurchaseOrdersByStatus(String status);

    // Get purchase orders by vendor
    List<PurchaseOrderDTO> getPurchaseOrdersByVendor(Integer vendorid);

    // Get purchase orders by event
    List<PurchaseOrderDTO> getPurchaseOrdersByEvent(Integer eventid);

    // Get purchase orders by cdsid
    List<PurchaseOrderDTO> getPurchaseOrdersByCdsid(String cdsid);

    // Get purchase orders by year
    List<PurchaseOrderDTO> getPurchaseOrdersByYear(int year);

    // Get purchase orders by date range
    List<PurchaseOrderDTO> getPurchaseOrdersByDateRange(Date startDate, Date endDate);

    // Get completed purchase orders
    List<PurchaseOrderDTO> getCompletedPurchaseOrders();

    // Get total order amount by vendor
    Double getTotalOrderAmountByVendor(Integer vendorid);

    // Create purchase order
    PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO dto);

    // Update purchase order
    PurchaseOrderDTO updatePurchaseOrder(Integer id, PurchaseOrderDTO dto);

    // Complete purchase order
    PurchaseOrderDTO completePurchaseOrder(Integer id);

    // Reject purchase order
    PurchaseOrderDTO rejectPurchaseOrder(Integer id);

    // Delete purchase order
    void deletePurchaseOrder(Integer id);
}
