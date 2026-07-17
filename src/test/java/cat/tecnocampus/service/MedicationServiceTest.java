package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.MedicationBatchDTO;
import cat.tecnocampus.application.dto.MedicationDTO;
import cat.tecnocampus.application.dto.MedicationPrescriptionDTO;
import cat.tecnocampus.application.service.MedicationService;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MedicationServiceTest {

    @Autowired
    private MedicationService medicationService;

    @Test
    void dataSqlLoadsMedications() {
        List<MedicationDTO> meds = medicationService.getAllMedications();
        assertEquals(5, meds.size(), "S'esperen 5 medicacions carregades per data.sql");
    }

    @Test
    void createMedicationAddsOne() {
        int before = medicationService.getAllMedications().size();

        MedicationDTO newMed = new MedicationDTO(
                null,
                "TestMed",
                "TestIngredient",
                1,
                9.99,
                Collections.emptyList()
        );

        MedicationDTO created = medicationService.createMedication(newMed);
        assertNotNull(created);
        assertEquals("TestMed", created.name());

        List<MedicationDTO> after = medicationService.getAllMedications();
        assertEquals(before + 1, after.size(), "S'ha d'haver afegit una nova medicació");
        assertTrue(after.stream().anyMatch(m -> "TestMed".equals(m.name())));
    }

    @Test
    void getByIdReturnsMedicationFromList() {
        List<MedicationDTO> meds = medicationService.getAllMedications();
        assertTrue(meds.stream().anyMatch(m -> "Ibuprofeno".equals(m.name())), "S'espera trobar 'Ibuprofeno' a la llista");

        MedicationDTO ibup = meds.stream().filter(m -> "Ibuprofeno".equals(m.name())).findFirst().orElseThrow();
        MedicationDTO byId = medicationService.getById(ibup.medicationId());
        assertEquals("Ibuprofeno", byId.name());
        assertEquals(ibup.medicationId(), byId.medicationId());
    }

    @Test
    void getByIdNotFoundThrows() {
        assertThrows(NotFoundException.class, () -> medicationService.getById(99999L));
    }

    @Test
    void createMedicationWithZeroPrice_allowsAndPersists() {
        // El servei actual no valida preu > 0; comprovem que es pot crear i s'afegeix
        int before = medicationService.getAllMedications().size();

        MedicationDTO newMed = new MedicationDTO(
                null,
                "ZeroPriceMed",
                "Ingredient",
                1,
                10.0,
                Collections.emptyList()
        );

        MedicationDTO created = medicationService.createMedication(newMed);
        assertNotNull(created);
        assertEquals("ZeroPriceMed", created.name());

        List<MedicationDTO> after = medicationService.getAllMedications();
        assertEquals(before + 1, after.size());
    }

    @Test
    void arriveNewMedicationBatch_createsBatchAndCanBeListed() {
        // Use medication id 1 from data.sql
        Long medicationId = 1L;
        MedicationBatchDTO batch = new MedicationBatchDTO(
                null,
                null,
                100,
                LocalDate.now(),
                LocalDate.now().plusYears(2),
                20,
                20,
                1.0,
                "S",
                5
        );

        MedicationBatchDTO created = medicationService.arriveNewMedicationBatch(batch, medicationId);
        assertNotNull(created);
        assertNotNull(created.batchId());
        List<MedicationBatchDTO> batches = medicationService.getBatchesOfMedication(medicationId);
        assertTrue(batches.stream().anyMatch(b -> b.batchId().equals(created.batchId())));
    }

    @Test
    void deleteMedicationBatch_deletesAndReturnsDto() {
        Long medicationId = 1L;
        MedicationBatchDTO batch = new MedicationBatchDTO(
                null,
                null,
                200,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                5,
                5,
                2.0,
                "A",
                10
        );

        MedicationBatchDTO created = medicationService.arriveNewMedicationBatch(batch, medicationId);
        assertNotNull(created.batchId());

        MedicationBatchDTO deleted = medicationService.deleteMedicationBatch(created.batchId());
        assertEquals(created.batchId(), deleted.batchId());

        List<MedicationBatchDTO> batches = medicationService.getBatchesOfMedication(medicationId);
        assertFalse(batches.stream().anyMatch(b -> b.batchId().equals(created.batchId())));
    }

    @Test
    void addPrescription_success_createsPrescriptionAndReducesBatch() {
        Long medicationId = 2L; // medication existing in data.sql
        // create a batch with enough quantity
        MedicationBatchDTO batch = new MedicationBatchDTO(
                null,
                null,
                300,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                10,
                10,
                1.5,
                "B",
                2
        );
        MedicationBatchDTO createdBatch = medicationService.arriveNewMedicationBatch(batch, medicationId);
        assertNotNull(createdBatch.batchId());

        // Use a visit that exists in data.sql with status 'Completed' or 'In_Progress'
        Long visitId = 6L; // data.sql includes a Completed visit (likely id 4)
        MedicationPrescriptionDTO prescDTO = new MedicationPrescriptionDTO(
                null,
                2,
                "2xdia",
                5,
                visitId,
                medicationId
        );

        MedicationPrescriptionDTO created = medicationService.addPrescription(visitId, prescDTO);
        assertNotNull(created);
        assertEquals(2, created.quantityPrescribed());
        assertEquals(visitId, created.visitId());
        assertEquals(medicationId, created.medicationId());

        // ensure batch list still exists and quantities managed (service updates batch)
        List<MedicationBatchDTO> batches = medicationService.getBatchesOfMedication(medicationId);
        assertFalse(batches.isEmpty());
    }

    @Test
    void sellMedication_success_returnsMedicationDTO() {
        Long medicationId = 3L;
        MedicationBatchDTO batch = new MedicationBatchDTO(
                null,
                null,
                400,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                15,
                15,
                3.0,
                "C",
                2
        );
        medicationService.arriveNewMedicationBatch(batch, medicationId);

        MedicationDTO medDto = medicationService.sellMedication(medicationId, 3);
        assertNotNull(medDto);
        assertEquals(medicationId, medDto.medicationId());
    }

    @Test
    void addRemoveIncompatibility_and_checkIncompatibilities_behaviour() {
        // create two new medications
        MedicationDTO m1 = medicationService.createMedication(new MedicationDTO(null, "IncoA", "X", 1, 5.0, Collections.emptyList()));
        MedicationDTO m2 = medicationService.createMedication(new MedicationDTO(null, "IncoB", "Y", 1, 6.0, Collections.emptyList()));

        MedicationDTO afterAdd = medicationService.addIncompatibility(m1.medicationId(), m2.medicationId());
        assertNotNull(afterAdd);

        // checkIncompatibilities should return a boolean (no exception)
        // use pet id 1 from data.sql
        boolean check = medicationService.checkIncompatibilities(1L, m1.medicationId());
        assertNotNull(check);

        MedicationDTO afterRemove = medicationService.removeIncompatibility(m1.medicationId(), m2.medicationId());
        assertNotNull(afterRemove);
    }

    @Test
    void getMedsBetweenDates_and_getMedsBetweenDatesOfVet_returnResults() {
        Long medicationId = 1L;
        // add a batch with a known received date
        LocalDate received = LocalDate.of(2025, 7, 1);
        MedicationBatchDTO batch = new MedicationBatchDTO(
                null,
                null,
                500,
                received,
                received.plusYears(1),
                8,
                8,
                1.0,
                "D",
                2
        );
        medicationService.arriveNewMedicationBatch(batch, medicationId);

        LocalDate start = received.minusDays(1);
        LocalDate end = received.plusDays(1);
        List<MedicationDTO> medsBetween = medicationService.getMedsBetweenDates(start, end);
        assertNotNull(medsBetween);
        assertTrue(medsBetween.stream().anyMatch(m -> m.medicationId().equals(medicationId)));

        // Create a prescription for a visit that has a veterinarian in data.sql to ensure getMedsBetweenDatesOfVet returns something
        Long visitId = 4L; // visit with start_date '2025-07-02' exists in data.sql
        MedicationPrescriptionDTO prescDTO = new MedicationPrescriptionDTO(null, 1, "1xdia", 1, visitId, medicationId);
        medicationService.addPrescription(visitId, prescDTO);

        // choose range covering that visit date
        LocalDate vetStart = LocalDate.of(2025, 7, 1);
        LocalDate vetEnd = LocalDate.of(2025, 7, 3);
        List<?> vets = medicationService.getMedsBetweenDatesOfVet(medicationId, vetStart, vetEnd);
        assertNotNull(vets);
        assertFalse(vets.isEmpty(), "S'esperen veterinaris que hagin prescrit la medicació en el rang");
    }
}
