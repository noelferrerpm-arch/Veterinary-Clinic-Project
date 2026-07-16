package cat.tecnocampus.api;

import cat.tecnocampus.application.dto.PetDTO;
import cat.tecnocampus.application.dto.PetOwnerDTO;
import cat.tecnocampus.application.inputDTO.PetInsertDTO;
import cat.tecnocampus.application.outputDTO.OwnerFidelityPointsDTO;
import cat.tecnocampus.application.service.PetService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pet")
public class PetRestController {

    private final PetService petService;

    public PetRestController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/{petId}")
    public PetDTO getPet(@PathVariable Long petId) {
        return this.petService.getPetById(petId);
    }

    @GetMapping("/owner/{petOwnerId}")
    public PetOwnerDTO getPetOwner(@PathVariable Long petOwnerId) {
        return this.petService.getPetOwnerById(petOwnerId);
    }

    @GetMapping("/owner/{petOwnerId}/fidelity")
    public OwnerFidelityPointsDTO getOwnerFidelityPoints(@PathVariable Long petOwnerId) {
        return this.petService.getLoyaltyPoints(petOwnerId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public PetDTO createPet(@RequestBody PetInsertDTO petDTO) {
       return this.petService.createPet(petDTO);
    }


    @PostMapping("/owner")
    @ResponseStatus(HttpStatus.CREATED)
    public PetOwnerDTO createPetOwner(@RequestBody PetOwnerDTO petOwnerDTO) {
        return this.petService.createPetOwner(petOwnerDTO);
    }

    @GetMapping("/{petId}/history")
    public List<Object> getPetHistory(@PathVariable Long petId) {
        return this.petService.getPetHistory(petId);
    }
}
