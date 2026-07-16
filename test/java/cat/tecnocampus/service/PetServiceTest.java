package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.PetDTO;
import cat.tecnocampus.application.dto.PetOwnerDTO;
import cat.tecnocampus.application.inputDTO.PetInsertDTO;
import cat.tecnocampus.application.outputDTO.OwnerFidelityPointsDTO;
import cat.tecnocampus.application.service.PetService;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PetServiceTest {

    @Autowired
    private PetService petService;

    @Test
    public void getPets_notEmpty_whenDataSqlLoaded() {
        List<PetDTO> pets = petService.getPets();
        assertNotNull(pets);
        assertFalse(pets.isEmpty(), "S'esperen mascotes carregades per data.sql");
        boolean hasBuddy = pets.stream().anyMatch(p -> "Buddy".equals(p.name()));
        assertTrue(hasBuddy, "Ha d'existir una mascota amb nom 'Buddy' segons data.sql");
    }

    @Test
    public void getPetById_existing_returnsPet() {
        PetDTO pet = petService.getPetById(1L);
        assertNotNull(pet);
        assertEquals("Buddy", pet.name());
        assertEquals(2L, pet.petOwnerId());
    }

    @Test
    public void getPetById_nonExisting_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> petService.getPetById(99999L));
    }

    @Test
    public void createPet_validData_createsAndReturnsPet() {
        String microchip = "mc-" + System.currentTimeMillis();
        PetInsertDTO dto = new PetInsertDTO(
                null,
                2L,    // petOwnerId existent a data.sql
                1L,    // petTypeId existent a data.sql
                "Lili",
                microchip,
                LocalDate.of(2020, 3, 3),
                "female",
                "Podenco",
                "white",
                "10 kg"
        );

        PetDTO created = petService.createPet(dto);
        assertNotNull(created);
        assertEquals("Lili", created.name());
        assertEquals(microchip, created.microchipId());
        assertEquals(2L, created.petOwnerId());
    }

    @Test
    public void createPet_invalidOwner_throwsNotFoundException() {
        PetInsertDTO dto = new PetInsertDTO(
                null,
                99999L,
                1L,
                "NoOwner",
                "mc-invalid-" + System.currentTimeMillis(),
                LocalDate.now(),
                "male",
                "Breed",
                "color",
                "1 kg"
        );
        assertThrows(NotFoundException.class, () -> petService.createPet(dto));
    }

    @Test
    public void getPetOwnerById_existing_returnsOwner() {
        PetOwnerDTO owner = petService.getPetOwnerById(2L);
        assertNotNull(owner);
        assertEquals("Bob", owner.firstName());
        assertEquals(50, owner.loyaltyPoints());
    }

    @Test
    public void createPetOwner_createsAndReturnsOwner_withLoyaltyTier() {
        PetOwnerDTO input = new PetOwnerDTO(
                0L,
                "Paco",
                "Sahur",
                "789769632",
                "paco+" + System.currentTimeMillis() + "@example.com",
                "Some street 21",
                999, // será sobrescrit a 0 pel servei, però es pot enviar qualsevol valor
                1L,  // loyalty tier existent a data.sql
                List.of()
        );

        PetOwnerDTO created = petService.createPetOwner(input);
        assertNotNull(created);
        assertEquals("Paco", created.firstName());
        // el servei fixa loyaltyPoints a 0 a la creació
        assertEquals(0, created.loyaltyPoints());
        assertEquals(1L, created.loyaltyTierId());
    }

    @Test
    public void getPetHistory_returnsHistoryList() {
        List<Object> history = petService.getPetHistory(1L);
        assertNotNull(history);
        assertFalse(history.isEmpty(), "Segons data.sql hi ha visites per la mascota 1");
    }

    @Test
    public void getLoyaltyPoints_returnsCorrectPoints() {
        OwnerFidelityPointsDTO dto = petService.getLoyaltyPoints(2L);
        assertNotNull(dto);
        // l'SQL inicial dona 50 punts al propietari amb person_id = 2
        assertEquals(50, dto.loyaltyPoints());
    }

    @Test
    public void getLoyaltyPoints_nonExistingOwner_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> petService.getLoyaltyPoints(99999L));
    }
}
