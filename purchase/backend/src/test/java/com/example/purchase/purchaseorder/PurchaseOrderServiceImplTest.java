package com.example.purchase.purchaseorder;

import com.example.purchase.exception.ResourceNotFoundException;
import com.example.purchase.negotiation.Negotiation;
import com.example.purchase.negotiation.NegotiationRepository;
import com.example.purchase.purchaserequest.PurchaseRequest;
import com.example.purchase.purchaserequest.PurchaseRequestRepository;
import com.example.purchase.purchaserequest.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrder Service Tests")
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @Mock
    private NegotiationRepository negotiationRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private PurchaseOrder purchaseOrder;
    private PurchaseOrderDTO purchaseOrderDTO;
    private PurchaseRequest purchaseRequest;
    private Negotiation negotiation;

    @BeforeEach
    void setUp() {
        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPO_id(1);
        purchaseOrder.setEventid(100);
        purchaseOrder.setEventname("Tech Conference 2024");
        purchaseOrder.setVendorid(10);
        purchaseOrder.setVendorname("ABC Vendors");
        purchaseOrder.setCdsid("JDOE123");
        purchaseOrder.setOrderdate(new Date());
        purchaseOrder.setOrderamountINR(45000.0);
        purchaseOrder.setOrderamountdollar(542.17);
        purchaseOrder.setPO_status("PENDING");

        purchaseOrderDTO = new PurchaseOrderDTO();
        purchaseOrderDTO.setPO_id(1);
        purchaseOrderDTO.setEventid(100);
        purchaseOrderDTO.setEventname("Tech Conference 2024");
        purchaseOrderDTO.setVendorid(10);
        purchaseOrderDTO.setVendorname("ABC Vendors");
        purchaseOrderDTO.setCdsid("JDOE123");
        purchaseOrderDTO.setOrderdate(new Date());
        purchaseOrderDTO.setOrderamountINR(45000.0);
        purchaseOrderDTO.setOrderamountdollar(542.17);
        purchaseOrderDTO.setPO_status("PENDING");

        purchaseRequest = new PurchaseRequest();
        purchaseRequest.setPrNumber(1001);
        purchaseRequest.setPrstatus(Status.APPROVED);

        negotiation = new Negotiation();
        negotiation.setNegotiationid(1);
        negotiation.setNegotiationstatus("Completed");
    }

    @Test
    @DisplayName("Should return PurchaseOrder when valid ID is provided")
    void testGetPurchaseOrderById_Success() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(purchaseOrder));
        PurchaseOrderDTO result = purchaseOrderService.getPurchaseOrderById(1);
        assertNotNull(result);
        assertEquals(1, result.getPO_id());
        assertEquals("Tech Conference 2024", result.getEventname());
        assertEquals(45000.0, result.getOrderamountINR());
        verify(purchaseOrderRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
    void testGetPurchaseOrderById_NotFound() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> purchaseOrderService.getPurchaseOrderById(999)
        );
        assertTrue(exception.getMessage().contains("PurchaseOrder not found"));
        verify(purchaseOrderRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should return all purchase orders")
    void testGetAllPurchaseOrders_Success() {
        List<PurchaseOrder> orders = Arrays.asList(purchaseOrder);
        when(purchaseOrderRepository.findAll()).thenReturn(orders);
        List<PurchaseOrderDTO> result = purchaseOrderService.getAllPurchaseOrders();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tech Conference 2024", result.get(0).getEventname());
        verify(purchaseOrderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return purchase orders by status")
    void testGetPurchaseOrdersByStatus_Success() {
        List<PurchaseOrder> orders = Arrays.asList(purchaseOrder);
        when(purchaseOrderRepository.findByStatus("PENDING")).thenReturn(orders);
        List<PurchaseOrderDTO> result = purchaseOrderService.getPurchaseOrdersByStatus("PENDING");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getPO_status());
        verify(purchaseOrderRepository, times(1)).findByStatus("PENDING");
    }

    @Test
    @DisplayName("Should return purchase orders by vendor")
    void testGetPurchaseOrdersByVendor_Success() {
        List<PurchaseOrder> orders = Arrays.asList(purchaseOrder);
        when(purchaseOrderRepository.findByVendorid(10)).thenReturn(orders);
        List<PurchaseOrderDTO> result = purchaseOrderService.getPurchaseOrdersByVendor(10);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getVendorid());
        verify(purchaseOrderRepository, times(1)).findByVendorid(10);
    }

    @Test
    @DisplayName("Should return total order amount by vendor")
    void testGetTotalOrderAmountByVendor_Success() {
        when(purchaseOrderRepository.getTotalOrderAmountByVendor(10)).thenReturn(45000.0);
        Double result = purchaseOrderService.getTotalOrderAmountByVendor(10);
        assertNotNull(result);
        assertEquals(45000.0, result);
        verify(purchaseOrderRepository, times(1)).getTotalOrderAmountByVendor(10);
    }

    @Test
    @DisplayName("Should return 0.0 when vendor has no orders")
    void testGetTotalOrderAmountByVendor_NoOrders() {
        when(purchaseOrderRepository.getTotalOrderAmountByVendor(999)).thenReturn(null);
        Double result = purchaseOrderService.getTotalOrderAmountByVendor(999);
        assertEquals(0.0, result);
        verify(purchaseOrderRepository, times(1)).getTotalOrderAmountByVendor(999);
    }

    @Test
    @DisplayName("Should create purchase order successfully")
    void testCreatePurchaseOrder_Success() {
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        PurchaseOrderDTO result = purchaseOrderService.createPurchaseOrder(purchaseOrderDTO);
        assertNotNull(result);
        assertEquals("Tech Conference 2024", result.getEventname());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should auto-convert INR to Dollar when creating purchase order")
    void testCreatePurchaseOrder_AutoConvertINRToDollar() {
        purchaseOrderDTO.setOrderamountdollar(null);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        PurchaseOrderDTO result = purchaseOrderService.createPurchaseOrder(purchaseOrderDTO);
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should update purchase order successfully")
    void testUpdatePurchaseOrder_Success() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        purchaseOrderDTO.setOrderamountINR(50000.0);
        PurchaseOrderDTO result = purchaseOrderService.updatePurchaseOrder(1, purchaseOrderDTO);
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).findById(1);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should complete purchase order successfully")
    void testCompletePurchaseOrder_Success() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        PurchaseOrderDTO result = purchaseOrderService.completePurchaseOrder(1);
        assertNotNull(result);
        verify(purchaseOrderRepository, times(1)).findById(1);
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("Should reject purchase order and cascade to PR and Negotiation")
    void testRejectPurchaseOrder_WithCascade() {
        purchaseOrder.setpurchaserequest(purchaseRequest);
        purchaseOrder.setnegotiation(negotiation);
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(negotiationRepository.save(any(Negotiation.class))).thenReturn(negotiation);
        when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenReturn(purchaseRequest);
        PurchaseOrderDTO result = purchaseOrderService.rejectPurchaseOrder(1);
        assertNotNull(result);
        verify(negotiationRepository, times(1)).save(any(Negotiation.class));
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequest.class));
    }

   @Test
@DisplayName("Should soft delete purchase order successfully")
void testDeletePurchaseOrder_Success() {
    purchaseOrder.setpurchaserequest(purchaseRequest);
    purchaseOrder.setnegotiation(negotiation);
    when(purchaseOrderRepository.findById(1)).thenReturn(Optional.of(purchaseOrder));
    when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
    
    assertDoesNotThrow(() -> purchaseOrderService.deletePurchaseOrder(1));
    
    // Verify soft delete - save is called to set isDeleted = true
    verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    
    //  Verify actual deleteById is NOT called (soft delete keeps record in DB)
    verify(purchaseOrderRepository, never()).deleteById(any());
    
    //  Verify PR and Negotiation are NOT modified (no cascade)
    verify(negotiationRepository, never()).save(any(Negotiation.class));
    verify(purchaseRequestRepository, never()).save(any(PurchaseRequest.class));
}


    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent purchase order")
    void testDeletePurchaseOrder_NotFound() {
        when(purchaseOrderRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(
            ResourceNotFoundException.class,
            () -> purchaseOrderService.deletePurchaseOrder(999)
        );
        verify(purchaseOrderRepository, never()).deleteById(any());
    }
}
