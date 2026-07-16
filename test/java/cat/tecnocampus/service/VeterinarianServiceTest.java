package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.VeterinarianDTO;
import cat.tecnocampus.application.service.VeterinarianService;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VeterinarianServiceTest {

    @Autowired
    private VeterinarianService veterinarianService;

    @Test
    public void getAllVeterinarian_notEmpty_whenDataSqlLoaded() {
        var vets = veterinarianService.getAllVeterinarian();
        assertNotNull(vets);
        assertFalse(vets.isEmpty(), "Se esperen veterinarios cargados por data.sql");
    }

    @Test
    public void getVeterinariansAvailable_returnsList() {
        var available = veterinarianService.getVeterinariansAvailable();
        assertNotNull(available);
    }

    @Test
    public void getVeterinarianDemand_validRange_returnsList() {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        var demand = veterinarianService.getVeterinarianDemand(start, end);
        assertNotNull(demand);
        // si data.sql contiene visitas programadas en este rango, la lista podrá ser no vacía
    }

    @Test
    public void getVeterinarianDemand_endBeforeStart_throwsInvalidDataException() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);
        assertThrows(InvalidDataException.class, () -> veterinarianService.getVeterinarianDemand(start, end));
    }

    @Test
    public void createVeterinarian_success_persistsAndReturnsDto() {
        String uniqueEmail = "vet+" + java.util.UUID.randomUUID() + "@example.test";

        VeterinarianDTO dto =
                new VeterinarianDTO(
                        null,
                        "TestFirst",
                        "TestLast",
                        "600000000",
                        uniqueEmail,
                        "Carrer Falsa 123",
                        3,
                        2,
                        null
                );

        var created = veterinarianService.createVeterinarian(dto);

        assertNotNull(created, "El VeterinarianDTO creado no debe ser null");
        assertEquals(uniqueEmail, created.email(), "El email debe coincidir");
        assertEquals(dto.firstName(), created.firstName(), "El firstName debe coincidir");
        assertEquals(dto.licenseNumber(), created.licenseNumber(), "El licenseNumber debe coincidir");
    }
}

