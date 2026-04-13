package com.healthcare.management_system.controller;

import com.healthcare.management_system.dto.SymptomBookingRequest;
import com.healthcare.management_system.service.TriageQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
// Add @CrossOrigin or configure CORS globally
public class TriageBookingController {

    private final TriageQueueService triageQueueService;

    @Autowired
    public TriageBookingController(TriageQueueService triageQueueService) {
        this.triageQueueService = triageQueueService;
    }

    @PostMapping("/triage")
    public ResponseEntity<?> submitTriageBooking(@RequestBody SymptomBookingRequest request) {
        // Validate payload security via standard tokens (assuming JWT filter handles auth)
        if(request.getPatientId() == null || request.getHospitalId() == null) {
            return ResponseEntity.badRequest().body("Missing required patient or hospital identifiers");
        }
        
        try {
            // Process booking with AI sentiment analysis and queue strategy calculations
            String bookingResult = triageQueueService.processBooking(request);
            
            // Return structured response
            return ResponseEntity.ok(new BookingResponse("SUCCESS", bookingResult));
        } catch(Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process booking request.");
        }
    }

    // Short-lived DTO for the response wrapper
    class BookingResponse {
        public String status;
        public String message;
        
        public BookingResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
