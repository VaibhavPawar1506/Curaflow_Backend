package com.healthcare.management_system.service;

import com.healthcare.management_system.dto.SymptomBookingRequest;
import org.springframework.stereotype.Service;

@Service
public class TriageQueueService {

    // Simulating an external AI Sentiment Analysis call
    public double analyzeSentimentScore(String textDescription) {
        // In a real scenario, this delegates to an NLP model or external API (e.g., OpenAI, HuggingFace).
        if (textDescription == null || textDescription.isEmpty()) return 0.0;
        
        String lowerCaseDesc = textDescription.toLowerCase();
        double baseScore = 0.0;
        
        // Very basic mock weighting based on keywords
        if (lowerCaseDesc.contains("panic") || lowerCaseDesc.contains("severe pain") || lowerCaseDesc.contains("chest pain") || lowerCaseDesc.contains("bleeding")) {
            baseScore += 50.0;
        } else if (lowerCaseDesc.contains("fever") || lowerCaseDesc.contains("vomiting") || lowerCaseDesc.contains("dizzy")) {
            baseScore += 30.0;
        } else {
            baseScore += 10.0;
        }
        
        return Math.min(baseScore, 100.0);
    }

    /**
     * Calculates the dynamic priority score.
     * Scale: 0 to 100 (Higher is more critical)
     */
    public double calculatePriorityScore(SymptomBookingRequest request) {
        // 1. Get base severity score from user input
        double userSeverityScore;
        switch (request.getUserSeverity()) {
            case HIGH:
                userSeverityScore = 40.0;
                break;
            case MEDIUM:
                userSeverityScore = 20.0;
                break;
            case LOW:
            default:
                userSeverityScore = 5.0;
                break;
        }

        // 2. Get AI Sentiment & Triage score
        double aiScore = analyzeSentimentScore(request.getDescription());
        
        // Optional: Weight the AI score against the user score.
        // E.g., User score represents 40%, AI analysis represents 60%.
        // Assuming AI score is 0-100:
        double finalPriority = userSeverityScore + (aiScore * 0.60);
        
        return Math.min(finalPriority, 100.0);
    }

    /**
     * Determines whether standard First-Come-First-Serve is overridden.
     */
    public String calculateQueuePosition(SymptomBookingRequest request) {
        double priorityScore = calculatePriorityScore(request);
        
        // Logic to push to queue:
        // priorityScore >= 75 -> EMERGENCY QUEUE (Overrides FCFS immediately)
        // priorityScore >= 40 -> URGENT QUEUE (Slipped ahead of standard)
        // Else -> STANDARD FCFS QUEUE
        
        if (priorityScore >= 75.0) {
            return "EMERGENCY_OVERRIDE_QUEUE";
        } else if (priorityScore >= 40.0) {
            return "URGENT_QUEUE";
        } else {
            return "STANDARD_FCFS_QUEUE";
        }
    }
    
    // Process the booking and place it in DB
    public String processBooking(SymptomBookingRequest request) {
        String assignedQueue = calculateQueuePosition(request);
        double calculatedScore = calculatePriorityScore(request);
        
        // TODO: Map to an Appointment Entity and save to Database via Repository
        // Example: Appointment appointment = new Appointment(request);
        // appointment.setPriorityScore(calculatedScore);
        // appointment.setQueueType(assignedQueue);
        // appointmentRepository.save(appointment);
        
        return "Booking Processed. Priority Score: " + calculatedScore + ". Assigned Queue: " + assignedQueue;
    }
}
