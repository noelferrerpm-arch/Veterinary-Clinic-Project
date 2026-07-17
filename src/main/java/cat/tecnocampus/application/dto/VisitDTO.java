package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.Status_VPR;

import java.time.LocalDateTime;

public record VisitDTO (
        Long visitId,
        LocalDateTime startDate,
        int duration,
        String reason,
        double price,
        String diagnosis,
        String notes,
        Status_VPR status,
        Long petId,
        Long ownerId,
        Long vetId,
        Long treatmentId,
        Long invoiceId
){

}
