package com.example.purchase.negotiation;

import com.example.purchase.exception.InvalidStatusTransitionException;
import com.example.purchase.exception.NegotiationAlreadyExistsException;
import com.example.purchase.exception.ResourceNotFoundException;
import com.example.purchase.purchaserequest.PurchaseRequest;
import com.example.purchase.purchaserequest.PurchaseRequestRepository;
import com.example.purchase.purchaserequest.Status;
import com.example.purchase.purchaseorder.PurchaseOrder;
import com.example.purchase.purchaseorder.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NegotiationServiceImpl implements NegotiationService {

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public List<NegotiationDTO> getAllNegotiations() {
        return negotiationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NegotiationDTO getNegotiationById(Integer id) {
        Negotiation negotiation = negotiationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation", "negotiationid", id));
        return convertToDTO(negotiation);
    }

    @Override
    public List<NegotiationDTO> getNegotiationsByStatus(String status) {
        return negotiationRepository.findByNegotiationstatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NegotiationDTO> getNegotiationsByVendor(Integer vendorid) {
        return negotiationRepository.findByVendorid(vendorid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NegotiationDTO> getNegotiationsByEvent(Integer eventid) {
        return negotiationRepository.findByEventid(eventid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NegotiationDTO> getNegotiationsByCdsid(String cdsid) {
        return negotiationRepository.findByCdsid(cdsid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NegotiationDTO> getNegotiationsByYear(int year) {
        return negotiationRepository.findByYear(year).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NegotiationDTO> getNegotiationsByDateRange(Date startDate, Date endDate) {
        return negotiationRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NegotiationDTO> getNegotiationsWithSavings() {
        return negotiationRepository.findNegotiationsWithSavings().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NegotiationDTO createNegotiation(NegotiationDTO dto) {
        Negotiation negotiation = new Negotiation();
        mapDTOToEntity(dto, negotiation);
        Negotiation saved = negotiationRepository.save(negotiation);
        return convertToDTO(saved);
    }

    @Override
    public NegotiationDTO createNegotiationFromPR(Integer prNumber) {
        // Find the Purchase Request
        PurchaseRequest pr = purchaseRequestRepository.findById(prNumber)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "prNumber", prNumber));

        // Check if PR status is PENDING
        if (!Status.PENDING.equals(pr.getPrstatus())) {
            throw new InvalidStatusTransitionException(
                    "Can only create negotiation from PENDING purchase requests. Current status: " + pr.getPrstatus());
        }

        // Check if negotiation already exists for this PR
        if (pr.getNegotiation() != null) {
            throw new NegotiationAlreadyExistsException(prNumber);
        }

        // Create new negotiation with PR data
        Negotiation negotiation = new Negotiation();
        negotiation.setEventid(pr.getEventid()); //  COPY FROM PR
        negotiation.setEventname(pr.getEventname()); //  COPY FROM PR
        negotiation.setVendorid(pr.getVendorid()); //  COPY FROM PR
        negotiation.setVendorname(pr.getVendorname()); //  COPY FROM PR
        negotiation.setCdsid(pr.getCdsid()); //  COPY FROM PR
        negotiation.setNegotiationdate(new Date());   // TODAY'S DATE
        negotiation.setInitialquoteamount(pr.getAllocatedamount()); //  COPY FROM PR
        negotiation.setFinalamount(pr.getAllocatedamount()); // Default to same as initial
        negotiation.setNegotiationstatus("Pending"); //  DEFAULT STATUS
        negotiation.setPurchaseRequest(pr);  //  LINK TO PR

        // SAVE TO DATABASE
        Negotiation saved = negotiationRepository.save(negotiation);
        System.out.println("Negotiation created from PR " + prNumber + " with ID: " + saved.getNegotiationid());
        // RETURN DTO TO FRONTEND
        return convertToDTO(saved);
    }

    @Override
    public NegotiationDTO updateNegotiation(Integer id, NegotiationDTO dto) {
        System.out.println("=== UPDATE NEGOTIATION STARTED ===");
        System.out.println("Negotiation ID: " + id);
        System.out.println("New Status: " + dto.getNegotiationstatus());

        Negotiation negotiation = negotiationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negotiation", "negotiationid", id));

        // Store old status 
        String oldStatus = negotiation.getNegotiationstatus();
        System.out.println("Old Status: " + oldStatus);

        // UPDATE NEGOTIATION FIELDS
        negotiation.setNegotiationdate(dto.getNegotiationdate());
        negotiation.setFinalamount(dto.getFinalamount());
        negotiation.setNegotiationstatus(dto.getNegotiationstatus());
        negotiation.setNotes(dto.getNotes());

        Negotiation updated = negotiationRepository.save(negotiation);
        System.out.println("Negotiation saved with new status: " + updated.getNegotiationstatus());

        // CHECK IF STATUS CHANGED TO "Completed"
        if (negotiation.getPurchaseRequest() != null) {
            PurchaseRequest pr = negotiation.getPurchaseRequest();

            // IF COMPLETED → CREATE PO
            if ("Completed".equals(dto.getNegotiationstatus()) &&
                    !"Completed".equals(oldStatus)) {

                System.out.println("=== UPDATING PURCHASE REQUEST TO APPROVED ===");
                System.out.println("Purchase Request Number: " + pr.getPrNumber());
                System.out.println("Current PR Status: " + pr.getPrstatus());

                pr.setPrstatus(Status.APPROVED);
                purchaseRequestRepository.save(pr);

                System.out.println("Purchase Request " + pr.getPrNumber() + " status updated to APPROVED"); // ✅ FIXED

                // AUTO-CREATE PURCHASE ORDER
                System.out.println("=== AUTO-CREATING PURCHASE ORDER ===");

                if (negotiation.getPurchaseOrder() == null) {
                    PurchaseOrder po = new PurchaseOrder();
                    // COPY DATA FROM NEGOTIATION
                    po.setEventid(negotiation.getEventid()); //  FROM NEGOTIATION
                    po.setEventname(negotiation.getEventname()); //  FROM NEGOTIATION
                    po.setVendorid(negotiation.getVendorid()); //  FROM NEGOTIATION
                    po.setVendorname(negotiation.getVendorname()); //  FROM NEGOTIATION
                    po.setCdsid(negotiation.getCdsid()); //  FROM NEGOTIATION
                    po.setOrderdate(new Date()); //  TODAY'S DATE
                    po.setOrderamountINR(negotiation.getFinalamount());  //  FINAL AMOUNT
                    po.setOrderamountdollar(negotiation.getFinalamount() / 83.0); // INR to USD conversion
                    po.setPO_status("PENDING"); //  DEFAULT STATUS
                    po.setpurchaserequest(pr); //  LINK TO PR
                    po.setnegotiation(updated); //  LINK TO NEGOTIATION
                    
                    //  SAVE PO TO DATABASE
                    PurchaseOrder savedPO = purchaseOrderRepository.save(po);
                    System.out.println("Purchase Order created with ID: " + savedPO.getPO_id());
                } else {
                    System.out.println("Purchase Order already exists for this Negotiation");
                }
            }

            // If Negotiation status changed to "Cancelled" → PR becomes "REJECTED"
            else if ("Cancelled".equals(dto.getNegotiationstatus()) &&
                    !"Cancelled".equals(oldStatus)) {

                System.out.println("=== UPDATING PURCHASE REQUEST TO REJECTED ===");
                System.out.println("Purchase Request Number: " + pr.getPrNumber());
                System.out.println("Current PR Status: " + pr.getPrstatus());

                pr.setPrstatus(Status.REJECTED);
                purchaseRequestRepository.save(pr);

                System.out.println("Purchase Request " + pr.getPrNumber() + " status updated to REJECTED");
            }
        }

        System.out.println("=== UPDATE NEGOTIATION ENDED ===");
        return convertToDTO(updated);
    }

    @Override
    public void deleteNegotiation(Integer id) {
        if (!negotiationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Negotiation", "negotiationid", id);
        }
        negotiationRepository.deleteById(id);
    }

    // ===========================
    // PRIVATE HELPER METHODS
    // ===========================

    // Convert entity to DTO
    private NegotiationDTO convertToDTO(Negotiation negotiation) {
        NegotiationDTO dto = new NegotiationDTO();
        dto.setNegotiationid(negotiation.getNegotiationid());
        dto.setEventid(negotiation.getEventid());
        dto.setEventname(negotiation.getEventname());
        dto.setVendorid(negotiation.getVendorid());
        dto.setVendorname(negotiation.getVendorname());
        dto.setCdsid(negotiation.getCdsid());
        dto.setNegotiationdate(negotiation.getNegotiationdate());
        dto.setInitialquoteamount(negotiation.getInitialquoteamount());
        dto.setFinalamount(negotiation.getFinalamount());
        dto.setNegotiationstatus(negotiation.getNegotiationstatus());
        dto.setNotes(negotiation.getNotes());
        if (negotiation.getPurchaseRequest() != null) {
            dto.setPrNumber(negotiation.getPurchaseRequest().getPrNumber());
        }
        return dto;
    }

    // Map DTO to entity
    private void mapDTOToEntity(NegotiationDTO dto, Negotiation negotiation) {
        negotiation.setEventid(dto.getEventid());
        negotiation.setEventname(dto.getEventname());
        negotiation.setVendorid(dto.getVendorid());
        negotiation.setVendorname(dto.getVendorname());
        negotiation.setCdsid(dto.getCdsid());
        negotiation.setNegotiationdate(dto.getNegotiationdate());
        negotiation.setInitialquoteamount(dto.getInitialquoteamount());
        negotiation.setFinalamount(dto.getFinalamount());
        negotiation.setNegotiationstatus(dto.getNegotiationstatus());
        negotiation.setNotes(dto.getNotes());

        if (dto.getPrNumber() != null) { //
            PurchaseRequest pr = purchaseRequestRepository.findById(dto.getPrNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "prNumber", dto.getPrNumber())); // ✅ FIXED
            negotiation.setPurchaseRequest(pr);
        }
    }
}
