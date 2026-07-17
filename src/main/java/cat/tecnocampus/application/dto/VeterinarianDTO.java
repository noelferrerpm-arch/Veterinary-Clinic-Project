package cat.tecnocampus.application.dto;

import java.util.List;

public record VeterinarianDTO(
    Long personId,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    String address,
    int licenseNumber,
    int yearsOfExperience,
    List<AvailabilityDTO> availabilityDTOList
) {
}
