package cat.tecnocampus.application.dto;
import java.util.List;
public record TreatmentPrescriptionDTO(
    long treatmentId,
    String name,
    String description,
    double cost
) {
}
