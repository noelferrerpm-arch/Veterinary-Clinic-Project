package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.PetDTO;
import cat.tecnocampus.application.dto.PetOwnerDTO;
import cat.tecnocampus.application.inputDTO.PetInsertDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.application.outputDTO.OwnerFidelityPointsDTO;
import cat.tecnocampus.domain.*;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PetService {
    private final PetOwnerRepository petOwnerRepository;
    private final PetRepository petRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final PetTypeRepository petTypeRepository;
    private final VisitRepository visitRepository;
    private final MedicationPrescriptionRepository medicationPrescriptionRepository;
    private final PersonRepository personRepository;

    public PetService(PetOwnerRepository petOwnerRepository, PetRepository petRepository, LoyaltyTierRepository loyaltyTierRepository, PetTypeRepository petTypeRepository, VisitRepository visitRepository, MedicationPrescriptionRepository medicationPrescriptionRepository, PersonRepository personRepository) {
        this.petOwnerRepository = petOwnerRepository;
        this.petRepository = petRepository;
        this.loyaltyTierRepository = loyaltyTierRepository;
        this.petTypeRepository = petTypeRepository;
        this.visitRepository = visitRepository;
        this.medicationPrescriptionRepository = medicationPrescriptionRepository;
        this.personRepository = personRepository;
    }

    public List<PetDTO> getPets() {
        return petRepository.findAll().stream().map(MapperHelper::mapPetDTO).toList();
    }


    public PetDTO getPetById(Long petId) {
        return MapperHelper.mapPetDTO(petRepository.findById(petId).orElseThrow(()-> new NotFoundException("Veterinarian not found")));
    }

    public PetDTO createPet(PetInsertDTO petDTO) {
        Pet pet = new Pet();
        PetOwner petOwner = this.petOwnerRepository.findById(petDTO.petOwnerId()).orElseThrow(()-> new NotFoundException("Pet Owner not found"));
        pet.setPetOwner(petOwner);
        PetType petType = this.petTypeRepository.findById(petDTO.petTypeId()).orElseThrow(()-> new NotFoundException("PetType not found"));
        pet.setPetType(petType);
        pet.setName(petDTO.name());
        pet.setMicrochipId(petDTO.microchipId());
        pet.setDateOfBirth(petDTO.dateOfBirth());
        pet.setGender(petDTO.gender());
        pet.setBreed(petDTO.breed());
        pet.setColor(petDTO.color());
        pet.setWeight(petDTO.weight());
        this.petRepository.save(pet);
        return MapperHelper.mapPetDTO(pet);
    }


    public PetOwnerDTO getPetOwnerById(Long petOwnerId) {
        return MapperHelper.mapPetOwnerDTO(petOwnerRepository.findById(petOwnerId).orElseThrow(()-> new NotFoundException("PetOwner not found")));
    }

    public PetOwnerDTO createPetOwner(PetOwnerDTO petOwnerDTO) {
        PetOwner petOwner = new PetOwner();
        petOwner.setLoyaltyPoints(0);
        petOwner.setFirstName(petOwnerDTO.firstName());
        petOwner.setLastName(petOwnerDTO.lastName());
        petOwner.setPhoneNumber(petOwnerDTO.phoneNumber());
        if(personRepository.existsByEmail(petOwnerDTO.email())) {
            throw new InvalidDataException("Email already in use");
        }
        petOwner.setEmail(petOwnerDTO.email());
        petOwner.setAddress(petOwnerDTO.address());

        LoyaltyTier lT = this.loyaltyTierRepository.findById(petOwnerDTO.loyaltyTierId()).orElseThrow(()-> new NotFoundException("LoyaltyTier not found"));
        petOwner.setLoyaltyTier(lT);

        this.petOwnerRepository.save(petOwner);
        return MapperHelper.mapPetOwnerDTO(petOwner);
    }

    public List<Object> getPetHistory(Long petId){
        List<Object> historial = new ArrayList<>();
        for(Visit v : visitRepository.findallVisitsByPet(petId)) {
            historial.add(MapperHelper.mapVisitDTO(v));
            if (v.getTreatment() != null) {
                historial.add(MapperHelper.mapTreatmentPrescriptionDTO(v.getTreatment()));
            }
            for(MedicationPrescription mp : medicationPrescriptionRepository.findAll()){
                if(mp.getVisit().equals(v)) {
                    historial.add(MapperHelper.mapMedicationPrescriptionDTO(mp));
                }
            }
        }
        return historial;
    }


    public OwnerFidelityPointsDTO getLoyaltyPoints(Long petOwnerId) {
        PetOwner petOwner = this.petOwnerRepository.findById(petOwnerId).orElseThrow(()-> new NotFoundException("Pet Owner not found"));
        int loyaltyPoints= petOwner.getLoyaltyPoints();
        return new OwnerFidelityPointsDTO(loyaltyPoints);
    }
}
