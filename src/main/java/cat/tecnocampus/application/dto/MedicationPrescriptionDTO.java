package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.Medication;
import cat.tecnocampus.domain.Visit;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;

public record MedicationPrescriptionDTO(
        Long prescriptionId,
        int quantityPrescribed,
        String dosageInstructions,
        int duration,
        Long visitId,
        Long medicationId
) {

}
