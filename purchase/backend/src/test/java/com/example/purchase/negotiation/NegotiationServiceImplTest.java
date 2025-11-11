package com.example.purchase.negotiation;

import com.example.purchase.exception.InvalidStatusTransitionException;
import com.example.purchase.exception.NegotiationAlreadyExistsException;
import com.example.purchase.exception.ResourceNotFoundException;
import com.example.purchase.purchaserequest.PurchaseRequest;
import com.example.purchase.purchaserequest.PurchaseRequestRepository;
import com.example.purchase.purchaserequest.Status;
import com.example.purchase.purchaseorder.PurchaseOrderRepository;
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
@DisplayName("Negotiation Service Tests")
class NegotiationServiceImplTest {

    @Mock
    private NegotiationRepository negotiationRepository;

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private NegotiationServiceImpl negotiationService;

    private Negotiation negotiation;
    private NegotiationDTO negotiationDTO;
    private PurchaseRequest purchaseRequest;

    @BeforeEach
    void setUp() {
        negotiation = new Negotiation();
        negotiation.setNegotiationid(1);
        negotiation.setEventid(100);
        negotiation.setEventname("Tech Conference 2024");
        negotiation.setVendorid(10);
        negotiation.setVendorname("ABC Vendors");
        negotiation.setCdsid("JDOE123");
        negotiation.setNegotiationdate(new Date());
        negotiation.setInitialquoteamount(50000.0);
        negotiation.setFinalamount(45000.0);
        negotiation.setNegotiationstatus("Pending");
        negotiation.setNotes("Initial negotiation");

        negotiationDTO = new NegotiationDTO();
        negotiationDTO.setNegotiationid(1);
        negotiationDTO.setEventid(100);
        negotiationDTO.setEventname("Tech Conference 2024");
        negotiationDTO.setVendorid(10);
        negotiationDTO.setVendorname("ABC Vendors");
        negotiationDTO.setCdsid("JDOE123");
        negotiationDTO.setNegotiationdate(new Date());
        negotiationDTO.setInitialquoteamount(50000.0);
        negotiationDTO.setFinalamount(45000.0);
        negotiationDTO.setNegotiationstatus("Pending");
        negotiationDTO.setNotes("Initial negotiation");

        purchaseRequest = new PurchaseRequest();
        purchaseRequest.setPrNumber(1001); //
        purchaseRequest.setEventid(100);
        purchaseRequest.setEventname("Tech Conference 2024");
        purchaseRequest.setVendorid(10);
        purchaseRequest.setVendorname("ABC Vendors");
        purchaseRequest.setCdsid("JDOE123");
        purchaseRequest.setRequestdate(new Date());
        purchaseRequest.setAllocatedamount(50000.0);
        purchaseRequest.setPrstatus(Status.PENDING);
    }

    @Test
    @DisplayName("Should return Negotiation when valid ID is provided")
    void testGetNegotiationById_Success() {
        when(negotiationRepository.findById(1)).thenReturn(Optional.of(negotiation));
        NegotiationDTO result = negotiationService.getNegotiationById(1);
        assertNotNull(result);
        assertEquals(1, result.getNegotiationid());
        assertEquals("Tech Conference 2024", result.getEventname());
        assertEquals(45000.0, result.getFinalamount());
        verify(negotiationRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
    void testGetNegotiationById_NotFound() {
        when(negotiationRepository.findById(999)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> negotiationService.getNegotiationById(999)
        );
        assertTrue(exception.getMessage().contains("Negotiation not found"));
        verify(negotiationRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should return all negotiations")
    void testGetAllNegotiations_Success() {
        List<Negotiation> negotiations = Arrays.asList(negotiation);
        when(negotiationRepository.findAll()).thenReturn(negotiations);
        List<NegotiationDTO> result = negotiationService.getAllNegotiations();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tech Conference 2024", result.get(0).getEventname());
        verify(negotiationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return negotiations by status")
    void testGetNegotiationsByStatus_Success() {
        List<Negotiation> negotiations = Arrays.asList(negotiation);
        when(negotiationRepository.findByNegotiationstatus("Pending")).thenReturn(negotiations);
        List<NegotiationDTO> result = negotiationService.getNegotiationsByStatus("Pending");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pending", result.get(0).getNegotiationstatus());
        verify(negotiationRepository, times(1)).findByNegotiationstatus("Pending");
    }

    @Test
    @DisplayName("Should return negotiations with savings")
    void testGetNegotiationsWithSavings_Success() {
        List<Negotiation> negotiations = Arrays.asList(negotiation);
        when(negotiationRepository.findNegotiationsWithSavings()).thenReturn(negotiations);
        List<NegotiationDTO> result = negotiationService.getNegotiationsWithSavings();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFinalamount() < result.get(0).getInitialquoteamount());
        verify(negotiationRepository, times(1)).findNegotiationsWithSavings();
    }

    @Test
    @DisplayName("Should create negotiation successfully")
    void testCreateNegotiation_Success() {
        when(negotiationRepository.save(any(Negotiation.class))).thenReturn(negotiation);
        NegotiationDTO result = negotiationService.createNegotiation(negotiationDTO);
        assertNotNull(result);
        assertEquals("Tech Conference 2024", result.getEventname());
        verify(negotiationRepository, times(1)).save(any(Negotiation.class));
    }

    @Test
    @DisplayName("Should create negotiation from Purchase Request successfully")
    void testCreateNegotiationFromPR_Success() {
        when(purchaseRequestRepository.findById(1001)).thenReturn(Optional.of(purchaseRequest));
        when(negotiationRepository.save(any(Negotiation.class))).thenReturn(negotiation);
        NegotiationDTO result = negotiationService.createNegotiationFromPR(1001);
        assertNotNull(result);
        verify(purchaseRequestRepository, times(1)).findById(1001);
        verify(negotiationRepository, times(1)).save(any(Negotiation.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when PR not found")
    void testCreateNegotiationFromPR_PRNotFound() {
        when(purchaseRequestRepository.findById(9999)).thenReturn(Optional.empty());
        assertThrows(
            ResourceNotFoundException.class,
            () -> negotiationService.createNegotiationFromPR(9999)
        );
        verify(negotiationRepository, never()).save(any(Negotiation.class));
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when PR is not PENDING")
    void testCreateNegotiationFromPR_PRNotPending() {
        purchaseRequest.setPrstatus(Status.APPROVED);
        when(purchaseRequestRepository.findById(1001)).thenReturn(Optional.of(purchaseRequest));
        assertThrows(
            InvalidStatusTransitionException.class,
            () -> negotiationService.createNegotiationFromPR(1001)
        );
        verify(negotiationRepository, never()).save(any(Negotiation.class));
    }

    @Test
    @DisplayName("Should throw NegotiationAlreadyExistsException when negotiation already exists for PR")
    void testCreateNegotiationFromPR_AlreadyExists() {
        purchaseRequest.setNegotiation(negotiation);
        when(purchaseRequestRepository.findById(1001)).thenReturn(Optional.of(purchaseRequest));
        assertThrows(
            NegotiationAlreadyExistsException.class,
            () -> negotiationService.createNegotiationFromPR(1001)
        );
        verify(negotiationRepository, never()).save(any(Negotiation.class));
    }

    @Test
    @DisplayName("Should update negotiation successfully")
    void testUpdateNegotiation_Success() {
        when(negotiationRepository.findById(1)).thenReturn(Optional.of(negotiation));
        when(negotiationRepository.save(any(Negotiation.class))).thenReturn(negotiation);
        negotiationDTO.setFinalamount(40000.0);
        NegotiationDTO result = negotiationService.updateNegotiation(1, negotiationDTO);
        assertNotNull(result);
        verify(negotiationRepository, times(1)).findById(1);
        verify(negotiationRepository, times(1)).save(any(Negotiation.class));
    }

    @Test
    @DisplayName("Should delete negotiation successfully")
    void testDeleteNegotiation_Success() {
        when(negotiationRepository.existsById(1)).thenReturn(true);
        doNothing().when(negotiationRepository).deleteById(1);
        assertDoesNotThrow(() -> negotiationService.deleteNegotiation(1));
        verify(negotiationRepository, times(1)).existsById(1);
        verify(negotiationRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent negotiation")
    void testDeleteNegotiation_NotFound() {
        when(negotiationRepository.existsById(999)).thenReturn(false);
        assertThrows(
            ResourceNotFoundException.class,
            () -> negotiationService.deleteNegotiation(999)
        );
        verify(negotiationRepository, never()).deleteById(any());
    }
}
