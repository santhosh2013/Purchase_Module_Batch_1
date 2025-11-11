package com.example.purchase.purchaserequest;

import com.example.purchase.exception.DuplicateEventException;
import com.example.purchase.exception.ResourceNotFoundException;
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
@DisplayName("PurchaseRequest Service Tests")
class PurchaseRequestServiceImplTest {

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @InjectMocks
    private PurchaseRequestServiceImpl purchaseRequestService;

    private PurchaseRequest purchaseRequest;
    private PurchaseRequestDTO purchaseRequestDTO;

    @BeforeEach
    void setUp() {
        purchaseRequest = new PurchaseRequest();
        purchaseRequest.setPrNumber(1001);
        purchaseRequest.setEventid(100);
        purchaseRequest.setEventname("Tech Conference 2024");
        purchaseRequest.setVendorid(10);
        purchaseRequest.setVendorname("ABC Vendors");
        purchaseRequest.setCdsid("JDOE123");
        purchaseRequest.setRequestdate(new Date());
        purchaseRequest.setAllocatedamount(50000.0);
        purchaseRequest.setPrstatus(Status.PENDING);

        purchaseRequestDTO = new PurchaseRequestDTO();
        purchaseRequestDTO.setPrNumber(1001);
        purchaseRequestDTO.setEventid(100);
        purchaseRequestDTO.setEventname("Tech Conference 2024");
        purchaseRequestDTO.setVendorid(10);
        purchaseRequestDTO.setVendorname("ABC Vendors");
        purchaseRequestDTO.setCdsid("JDOE123");
        purchaseRequestDTO.setRequestdate(new Date());
        purchaseRequestDTO.setAllocatedamount(50000.0);
        purchaseRequestDTO.setPrstatus("PENDING");
    }

    @Test
    @DisplayName("Should return PurchaseRequest when valid PR Number is provided")
    void testGetPurchaseRequestById_Success() {
        // Arrange
        when(purchaseRequestRepository.findById(1001)).thenReturn(Optional.of(purchaseRequest));

        // Act
        PurchaseRequestDTO result = purchaseRequestService.getPurchaseRequestById(1001);

        // Assert
        assertNotNull(result);
        assertEquals(1001, result.getPrNumber());
        assertEquals("Tech Conference 2024", result.getEventname());
        assertEquals(50000.0, result.getAllocatedamount());
        verify(purchaseRequestRepository, times(1)).findById(1001);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when PR Number does not exist")
    void testGetPurchaseRequestById_NotFound() {
        // Arrange
        when(purchaseRequestRepository.findById(9999)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> purchaseRequestService.getPurchaseRequestById(9999)
        );
        
        assertTrue(exception.getMessage().contains("PurchaseRequest not found"));
        verify(purchaseRequestRepository, times(1)).findById(9999);
    }

    @Test
    @DisplayName("Should delete PurchaseRequest successfully when PR Number exists")
    void testDeletePurchaseRequest_Success() {
        // Arrange
        when(purchaseRequestRepository.existsById(1001)).thenReturn(true);
        doNothing().when(purchaseRequestRepository).deleteById(1001);

        // Act & Assert
        assertDoesNotThrow(() -> purchaseRequestService.deletePurchaseRequest(1001));
        verify(purchaseRequestRepository, times(1)).existsById(1001);
        verify(purchaseRequestRepository, times(1)).deleteById(1001);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent PR Number")
    void testDeletePurchaseRequest_NotFound() {
        // Arrange
        when(purchaseRequestRepository.existsById(9999)).thenReturn(false);

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> purchaseRequestService.deletePurchaseRequest(9999)
        );
        
        verify(purchaseRequestRepository, times(1)).existsById(9999);
        verify(purchaseRequestRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should return all purchase requests")
    void testGetAllPurchaseRequests_Success() {
        // Arrange
        List<PurchaseRequest> requests = Arrays.asList(purchaseRequest);
        when(purchaseRequestRepository.findAll()).thenReturn(requests);

        // Act
        List<PurchaseRequestDTO> result = purchaseRequestService.getAllPurchaseRequests();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tech Conference 2024", result.get(0).getEventname());
        verify(purchaseRequestRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create purchase request successfully")
    void testCreatePurchaseRequest_Success() {
        // Arrange
        when(purchaseRequestRepository.existsById(1001)).thenReturn(false);
        when(purchaseRequestRepository.findAll()).thenReturn(Arrays.asList());
        when(purchaseRequestRepository.save(any(PurchaseRequest.class))).thenReturn(purchaseRequest);

        // Act
        PurchaseRequestDTO result = purchaseRequestService.createPurchaseRequest(purchaseRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Tech Conference 2024", result.getEventname());
        verify(purchaseRequestRepository, times(1)).save(any(PurchaseRequest.class));
    }



   @Test
@DisplayName("Should throw DuplicateEventException when event ID already exists")
void testCreatePurchaseRequest_DuplicateEventId() {
    // Create an existing PR with DIFFERENT PR Number but SAME Event ID
    PurchaseRequest existingPR = new PurchaseRequest();
    existingPR.setPrNumber(2001); // Different PR Number
    existingPR.setEventid(100);    // Same Event ID as the new request
    existingPR.setEventname("Tech Conference 2024");
    existingPR.setVendorid(10);
    existingPR.setVendorname("ABC Vendors");
    existingPR.setCdsid("JDOE123");
    existingPR.setRequestdate(new Date());
    existingPR.setAllocatedamount(50000.0);
    existingPR.setPrstatus(Status.PENDING);
    
    // Arrange
    when(purchaseRequestRepository.existsById(1001)).thenReturn(false); // PR Number 1001 doesn't exist
    when(purchaseRequestRepository.findAll()).thenReturn(Arrays.asList(existingPR)); // But Event ID 100 exists

    // Act & Assert
    DuplicateEventException exception = assertThrows(
        DuplicateEventException.class,
        () -> purchaseRequestService.createPurchaseRequest(purchaseRequestDTO)
    );
    
assertTrue(exception.getMessage().contains("G cross number 100 already exists"));

    verify(purchaseRequestRepository, never()).save(any(PurchaseRequest.class));
}
}
