package cat.tecnocampus.api;
import cat.tecnocampus.application.dto.MedicationBatchDTO;
import cat.tecnocampus.application.dto.MedicationDTO;
import cat.tecnocampus.application.dto.VeterinarianDTO;
import cat.tecnocampus.application.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medication")
public class MedicationRestController {
    private final MedicationService medicationService;


    public MedicationRestController(MedicationService medicationBatchService) {
        this.medicationService = medicationBatchService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicationDTO createMedication(@RequestBody @Valid MedicationDTO medication) {
        return this.medicationService.createMedication(medication);
    }
    @GetMapping
    public List<MedicationDTO> getAllMedications() {
        return this.medicationService.getAllMedications();
    }

    @GetMapping("/{medicationId}")
    public MedicationDTO getMedicationById(@PathVariable long medicationId) {
        return this.medicationService.getById(medicationId);
    }


    @PostMapping("/{medicationID}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicationBatchDTO createMedicationBatch(@RequestBody @Valid MedicationBatchDTO medicationBatch,@PathVariable Long medicationID) {
        return this.medicationService.arriveNewMedicationBatch(medicationBatch, medicationID);
    }

    @GetMapping("/{medicationID}/buy/{quantity}")
    public MedicationDTO sellMedication(@PathVariable Long medicationID, @PathVariable int quantity){
        return this.medicationService.sellMedication(medicationID, quantity);
    }

    @DeleteMapping("/batches/{target_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public MedicationBatchDTO deleteMedicationBatch(@PathVariable Long target_id) {
        return this.medicationService.deleteMedicationBatch(target_id);
    }


    @PutMapping("/{medicationID}/incompatibility/add/{incompatibilityID}")
    public MedicationDTO addIncompatibility(@PathVariable Long medicationID, @PathVariable Long incompatibilityID) {
        return this.medicationService.addIncompatibility(medicationID, incompatibilityID);
    }

    @PutMapping("/{medicationID}/incompatibility/remove/{incompatibilityID}")
    public MedicationDTO removeIncompatibility(@PathVariable Long medicationID, @PathVariable Long incompatibilityID) {
        return this.medicationService.removeIncompatibility(medicationID, incompatibilityID);
    }

    @GetMapping("/batches/{medicationID}")
    public List<MedicationBatchDTO> getAllMedicationBatches(@PathVariable Long medicationID) {
        return this.medicationService.getBatchesOfMedication(medicationID);
    }


    @GetMapping("/start/{start_date}/end/{end_date}")
    public List<MedicationDTO> getMedsBetweenDates(@PathVariable LocalDate start_date, @PathVariable LocalDate end_date){
        return this.medicationService.getMedsBetweenDates(start_date, end_date);
    }

    @GetMapping("/{medicationID}/start/{start_date}/end/{end_date}")
    public List<VeterinarianDTO> getMedsBetweenDatesOfVet(@PathVariable Long medicationID, @PathVariable LocalDate start_date, @PathVariable LocalDate end_date){
        return this.medicationService.getMedsBetweenDatesOfVet(medicationID,start_date, end_date);
    }

}
