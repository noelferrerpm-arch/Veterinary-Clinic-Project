package cat.tecnocampus.application.inputDTO;

import cat.tecnocampus.application.dto.PetOwnerDTO;
import cat.tecnocampus.application.dto.PetTypeDTO;

import java.time.LocalDate;

public record PetInsertDTO(
        Long petId,
        Long petOwnerId,
        Long petTypeId,
        String name,
        String microchipId,
        LocalDate dateOfBirth,
        String gender,
        String breed,
        String color,
        String weight
) {
}


