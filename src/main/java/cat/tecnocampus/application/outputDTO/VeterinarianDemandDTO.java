package cat.tecnocampus.application.outputDTO;

public record VeterinarianDemandDTO(
        Long personId,
        String vet_name,
        Long countVisits
) {
}
