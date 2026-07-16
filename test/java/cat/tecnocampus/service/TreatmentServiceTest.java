package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.TreatmentPrescriptionDTO;
import cat.tecnocampus.application.service.TreatmentService;
import cat.tecnocampus.domain.Status_VPR;
import cat.tecnocampus.domain.Treatment;
import cat.tecnocampus.domain.Visit;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.TreatmentRepository;
import cat.tecnocampus.persistence.VisitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TreatmentServiceTest {

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Test
    public void createTreatment_validVisitCompleted_returnsDTO() {
        Optional<Visit> maybe = visitRepository.findAll().stream()
                .filter(v -> v.getStatus() == Status_VPR.Completed || v.getStatus() == Status_VPR.In_Progress)
                .findFirst();

        if (maybe.isEmpty()) {
            fail("No se encontró ninguna Visit en estado Completed o In_Progress en la DB de pruebas (revisa data.sql).");
            return;
        }

        Visit visit = maybe.get();
        TreatmentPrescriptionDTO dto = new TreatmentPrescriptionDTO(0L, "TratamientoTest", "Descripción", 42.5);

        TreatmentPrescriptionDTO saved = treatmentService.createTreatment(visit.getVisitId(), dto);

        assertNotNull(saved);
        assertEquals("TratamientoTest", saved.name());
        assertEquals("Descripción", saved.description());
        assertEquals(42.5, saved.cost(), 0.0001);

        // verificar que se persistió
        assertTrue(treatmentRepository.findById(saved.treatmentId()).isPresent());
    }

    @Test
    public void createTreatment_visitNotFound_throwsNotFoundException() {
        TreatmentPrescriptionDTO dto = new TreatmentPrescriptionDTO(0L, "X", "Y", 10.0);
        assertThrows(NotFoundException.class, () -> treatmentService.createTreatment(999999L, dto));
    }

    @Test
    public void createTreatment_visitWrongStatus_throwsInvalidDataException() {
        Optional<Visit> maybeScheduled = visitRepository.findAll().stream()
                .filter(v -> v.getStatus() != Status_VPR.Completed && v.getStatus() != Status_VPR.In_Progress)
                .findFirst();

        if (maybeScheduled.isEmpty()) {
            fail("No se encontró ninguna Visit en estado distinto a Completed/In_Progress en la DB de pruebas (revisa data.sql).");
            return;
        }

        Visit visit = maybeScheduled.get();
        TreatmentPrescriptionDTO dto = new TreatmentPrescriptionDTO(0L, "Bad", "Bad", 5.0);

        assertThrows(InvalidDataException.class, () -> treatmentService.createTreatment(visit.getVisitId(), dto));
    }
}

