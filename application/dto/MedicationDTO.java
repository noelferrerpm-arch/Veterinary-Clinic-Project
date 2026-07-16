package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.Medication;
import jakarta.persistence.Column;

import java.util.List;

public record MedicationDTO(Long medicationId, String name, String activeIngredient, int dosageUnit, double unitPrice, List<Medication> incompatibilities) {
}

