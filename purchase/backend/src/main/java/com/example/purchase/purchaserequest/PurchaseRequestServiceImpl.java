package com.example.purchase.purchaserequest;

import com.example.purchase.exception.*;
import com.example.purchase.exception.DuplicateEventException;
import com.example.purchase.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;

    @Override
    public List<PurchaseRequestDTO> getAllPurchaseRequests() {
        //  FETCH ALL FROM DATABASE
        return purchaseRequestRepository.findAll().stream()
                //  CONVERT EACH ENTITY TO DTO
                .map(this::convertToDTO)
                //  COLLECT AS LIST
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseRequestDTO getPurchaseRequestById(Integer id) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "prNumber", id));
        return convertToDTO(pr);
    }

    @Override
    public List<PurchaseRequestDTO> getPurchaseRequestsByStatus(Status status) {
        return purchaseRequestRepository.findByPrstatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getPurchaseRequestsByVendor(Integer vendorid) {
        return purchaseRequestRepository.findByVendorid(vendorid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getPurchaseRequestsByEvent(Integer eventid) {
        return purchaseRequestRepository.findByEventid(eventid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getPurchaseRequestsByCdsid(String cdsid) {
        return purchaseRequestRepository.findByCdsid(cdsid).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getPurchaseRequestsByYear(int year) {
        return purchaseRequestRepository.findByYear(year).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getPurchaseRequestsByDateRange(Date startDate, Date endDate) {
        return purchaseRequestRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getPendingPurchaseRequests() {
        return purchaseRequestRepository.findPendingRequests().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseRequestDTO> getApprovedPurchaseRequests() {
        return purchaseRequestRepository.findApprovedRequests().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseRequestDTO createPurchaseRequest(PurchaseRequestDTO dto) {
        //  Validation 1: Check for duplicate PR Number
        validatePRNumberUniqueness(dto.getPrNumber());

        //  Validation 2: Check for duplicate Event ID (G cross number)
        validateEventIdUniqueness(dto.getEventid());

        //  Create entity from DTO
        PurchaseRequest pr = new PurchaseRequest();
        pr.setPrNumber(dto.getPrNumber());
        pr.setEventid(dto.getEventid());
        pr.setEventname(dto.getEventname());
        pr.setVendorid(dto.getVendorid());
        pr.setVendorname(dto.getVendorname());
        pr.setCdsid(dto.getCdsid());
        pr.setRequestdate(dto.getRequestdate());
        pr.setAllocatedamount(dto.getAllocatedamount());
        pr.setPrstatus(Status.valueOf(dto.getPrstatus()));

         //  SAVE TO DATABASE
        PurchaseRequest saved = purchaseRequestRepository.save(pr);
         //  RETURN DTO TO FRONTEND
        return convertToDTO(saved);
    }

    @Override
    public PurchaseRequestDTO updatePurchaseRequest(Integer id, PurchaseRequestDTO dto) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "prNumber", id));
        
        // Don't allow changing PR Number
        dto.setPrNumber(id);
        
        mapDTOToEntity(dto, pr);
        PurchaseRequest updated = purchaseRequestRepository.save(pr);
        return convertToDTO(updated);
    }

    @Override
    public PurchaseRequestDTO approvePurchaseRequest(Integer id) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "prNumber", id));
        pr.setPrstatus(Status.APPROVED);
        PurchaseRequest updated = purchaseRequestRepository.save(pr);
        return convertToDTO(updated);
    }

    @Override
    public PurchaseRequestDTO rejectPurchaseRequest(Integer id) {
        PurchaseRequest pr = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequest", "prNumber", id));
        pr.setPrstatus(Status.REJECTED);
        PurchaseRequest updated = purchaseRequestRepository.save(pr);
        return convertToDTO(updated);
    }

    @Override
    public void deletePurchaseRequest(Integer id) {
        if (!purchaseRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("PurchaseRequest", "prNumber", id);
        }
        purchaseRequestRepository.deleteById(id);
    }


    private void validatePRNumberUniqueness(Integer prNumber) throws DuplicatePRNumberException {
        if (purchaseRequestRepository.existsById(prNumber)) {
            throw new DuplicatePRNumberException(prNumber);
        }
    }

    private void validateEventIdUniqueness(Integer eventid) {
        boolean eventExists = purchaseRequestRepository.findAll().stream()
                .anyMatch(pr -> pr.getEventid().equals(eventid));

        if (eventExists) {
            throw new DuplicateEventException(eventid);
        }
    }


    private PurchaseRequestDTO convertToDTO(PurchaseRequest pr) {
        PurchaseRequestDTO dto = new PurchaseRequestDTO();
        dto.setPrNumber(pr.getPrNumber());
        dto.setEventid(pr.getEventid());
        dto.setEventname(pr.getEventname());
        dto.setVendorid(pr.getVendorid());
        dto.setVendorname(pr.getVendorname());
        dto.setCdsid(pr.getCdsid());
        dto.setRequestdate(pr.getRequestdate());
        dto.setAllocatedamount(pr.getAllocatedamount());
        dto.setPrstatus(pr.getPrstatus().toString());
        
        // Include negotiation ID if exists
        if (pr.getNegotiation() != null) {
            dto.setNegotiationid(pr.getNegotiation().getNegotiationid());
        }
        
        return dto;
    }


    private void mapDTOToEntity(PurchaseRequestDTO dto, PurchaseRequest pr) {
        pr.setPrNumber(dto.getPrNumber());
        pr.setEventid(dto.getEventid());
        pr.setEventname(dto.getEventname());
        pr.setVendorid(dto.getVendorid());
        pr.setVendorname(dto.getVendorname());
        pr.setCdsid(dto.getCdsid());
        pr.setRequestdate(dto.getRequestdate());
        pr.setAllocatedamount(dto.getAllocatedamount());
        
        if (dto.getPrstatus() != null) {
            pr.setPrstatus(Status.valueOf(dto.getPrstatus()));
        }
    }
}
