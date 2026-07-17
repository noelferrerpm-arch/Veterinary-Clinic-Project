package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.AvailabilityDTO;
import cat.tecnocampus.application.dto.AvailabilityExceptionDTO;
import cat.tecnocampus.application.service.AvailabilityService;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AvailabilityServiceTest {

    @Autowired
    private AvailabilityService availabilityService;

    @Test
    public void getAvailabilities_notEmpty_whenDataSqlLoaded() {
        List<AvailabilityDTO> availabilities = availabilityService.getAvailabilities(1L);
        assertNotNull(availabilities);
        assertFalse(availabilities.isEmpty(), "Se esperan availabilities cargadas por data.sql para veterinarian id 1");
    }

    @Test
    public void getAvailabilityById_returnsDTO() {
        // data.sql inserta varias availability para veterinarian 1; se asume que existe la id 1
        AvailabilityDTO dto = availabilityService.getAvailabilityById(1L, 1L);
        assertNotNull(dto);
        assertEquals(1, dto.dayOfWeek(), "El dayOfWeek de la availability 1 debe ser válido");
    }

    @Test
    public void createAvailability_createsAndReturnsDTO_thenDelete() {
        AvailabilityDTO newAvailability = new AvailabilityDTO(
                null,
                1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null
        );


        AvailabilityDTO created = availabilityService.createAvailability(1L, newAvailability);
        assertNotNull(created);
        assertEquals(newAvailability.dayOfWeek(), created.dayOfWeek());
        assertEquals(newAvailability.startTime(), created.startTime());
        assertEquals(newAvailability.endTime(), created.endTime());

        // Comprobar que la availability existe inicialmente
        assertDoesNotThrow(() -> availabilityService.getAvailabilityById(1L, 2L));

        // Eliminar la availability
        availabilityService.deleteAvailability(1L, 2L);

        // Tras la eliminación, obtenerla debe lanzar NotFoundException
        assertThrows(NotFoundException.class, () -> availabilityService.getAvailabilityById(1L, 2L));
    }


    @Test
    public void createAvailabilityException_createsAndReturnsDTO_thenDelete() {
        AvailabilityExceptionDTO exceptionDTO = new AvailabilityExceptionDTO(
                null,
                "Causa de prueba",
                1,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        AvailabilityExceptionDTO created = availabilityService.createAvailabilityException(1L, 1L, exceptionDTO);
        assertNotNull(created);
        assertEquals("Causa de prueba", created.reason());
        assertEquals(exceptionDTO.dayOfWeek(), created.dayOfWeek());

        // Eliminar la excepción recién creada (su id será 1 al no haber otras en data.sql)
        availabilityService.deleteAvailabilityException(1L, 1L, 1L);

        // Intentar eliminar de nuevo debe lanzar NotFoundException
        assertThrows(NotFoundException.class, () -> availabilityService.deleteAvailabilityException(1L, 1L, 1L));
    }

}
