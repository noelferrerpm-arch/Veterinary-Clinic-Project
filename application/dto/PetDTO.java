package cat.tecnocampus.application.dto;

import java.time.LocalDate;

public record PetDTO(
    Long petId,
    Long petOwnerId,
    PetTypeDTO petType,
    String name,
    String microchipId,
    LocalDate dateOfBirth,
    String gender,
    String breed,
    String color,
    String weight
) {
}
