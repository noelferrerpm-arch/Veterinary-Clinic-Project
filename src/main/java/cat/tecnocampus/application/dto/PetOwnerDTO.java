package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.LoyaltyTier;
import java.util.List;
public record PetOwnerDTO(
        long personId,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String address,
        int loyaltyPoints,
        long loyaltyTierId,
        List<PetDTO> pets
) {
}
