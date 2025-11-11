package com.example.purchase.negotiation;

import java.util.Date;
import java.util.List;

public interface NegotiationService {

    // Get all negotiations
    List<NegotiationDTO> getAllNegotiations();

    // Get negotiation by id
    NegotiationDTO getNegotiationById(Integer id);

    // Get negotiations by status
    List<NegotiationDTO> getNegotiationsByStatus(String status);

    // Get negotiations by vendor
    List<NegotiationDTO> getNegotiationsByVendor(Integer vendorid);

    // Get negotiations by event
    List<NegotiationDTO> getNegotiationsByEvent(Integer eventid);

    // Get negotiations by cdsid
    List<NegotiationDTO> getNegotiationsByCdsid(String cdsid);

    // Get negotiations by year
    List<NegotiationDTO> getNegotiationsByYear(int year);

    // Get negotiations by date range
    List<NegotiationDTO> getNegotiationsByDateRange(Date startDate, Date endDate);

    // Get negotiations with savings
    List<NegotiationDTO> getNegotiationsWithSavings();

    // Create negotiation
    NegotiationDTO createNegotiation(NegotiationDTO dto);

    // Create negotiation from Purchase Request
    NegotiationDTO createNegotiationFromPR(Integer prid);

    // Update negotiation
    NegotiationDTO updateNegotiation(Integer id, NegotiationDTO dto);

    // Delete negotiation
    void deleteNegotiation(Integer id);
}


