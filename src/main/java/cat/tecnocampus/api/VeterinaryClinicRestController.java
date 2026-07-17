package cat.tecnocampus.api;
import cat.tecnocampus.application.dto.*;
import cat.tecnocampus.application.outputDTO.VeterinarianDemandDTO;
import cat.tecnocampus.application.inputDTO.scheduleVisitDTO;
import cat.tecnocampus.application.service.TreatmentService;
import cat.tecnocampus.application.service.VeterinarianService;
import cat.tecnocampus.application.service.MedicationService;
import cat.tecnocampus.application.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/veterinarian")
public class VeterinaryClinicRestController {

    private final MedicationService medicationService;
    private final TreatmentService treatmentService;
    private final VeterinarianService veterinarianService;
    private final VisitService visitService;

    public VeterinaryClinicRestController(VeterinarianService veterinarianService, VisitService visitService, MedicationService medicationService, TreatmentService treatmentService) {
        this.veterinarianService = veterinarianService;
        this.visitService = visitService;
        this.medicationService= medicationService;
        this.treatmentService=treatmentService;
    }

    @GetMapping
    public List<VeterinarianDTO> getVeterinarian() {
        return this.veterinarianService.getAllVeterinarian();
    }

    @GetMapping("/available")
    public List<VeterinarianDTO> getVeterinarianAvailable() {
        return this.veterinarianService.getVeterinariansAvailable();
    }

    @GetMapping("/{vetId}/agenda")
    public List<AgendaDTO> getAgendaByVetId(@PathVariable Long vetId) {
        return this.visitService.getAgendaByVetId(vetId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public VeterinarianDTO createVeterinarian(@RequestBody @Valid VeterinarianDTO veterinarian) {
        return this.veterinarianService.createVeterinarian(veterinarian);
    }

    @PostMapping("/visit")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDTO createVisit(@RequestBody @Valid scheduleVisitDTO visitDTO) {
        return this.visitService.createVisit(visitDTO);
    }

    @PutMapping("/visits/{visitId}/initiate")
    public VisitDTO initiateVisit(@PathVariable Long visitId) {
        return this.visitService.initiateVisit(visitId);
    }

    @PutMapping("/visits/{visitId}/complete")
    public VisitDTO completeVisit(@PathVariable Long visitId) {
        return this.visitService.completeVisit(visitId);
    }

    @PatchMapping("/visits/{visitId}/diagnosis")
    public VisitDTO addDiagnosis(@PathVariable Long visitId, @RequestBody Map<String, String> bodyDiagnosis) {
        return this.visitService.addDiagnosis(visitId, bodyDiagnosis);
    }
    @PostMapping("/{vetId}/visits/walk-in")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDTO createVisitWalkIn(@PathVariable Long vetId, @RequestBody scheduleVisitDTO visitDTO) {
        return this.visitService.createVisitWalkIn(vetId, visitDTO);
    }

    @PutMapping("/visits/{visitId}")
    public VisitDTO reescheduleVisit(@PathVariable Long visitId, @RequestBody scheduleVisitDTO visitDTO){
        return this.visitService.reescheduleVisit(visitId, visitDTO);
    }

    @DeleteMapping("/visits/{visitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVisit(@PathVariable Long visitId) {
        this.visitService.deleteVisit(visitId);
    }

    @GetMapping("/visits/{visitId}")
    public VisitDTO getVisit(@PathVariable Long visitId) {
        return this.visitService.getVisit(visitId);
    }

    @GetMapping("/visits")
    public List<VisitDTO> getAllVisits() {
        return this.visitService.getAllVisits();
    }

    @GetMapping("/{vetId}/visits/start/{start_date}/end/{end_date}")
     public List<VisitDTO> getVisitsBetweenDates(@PathVariable Long vetId, @PathVariable LocalDate start_date, @PathVariable LocalDate end_date){
      return this.visitService.getVisitsBetweenDates(vetId, start_date, end_date);
    }

    @PostMapping("visits/{visitId}/prescribe-medication")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicationPrescriptionDTO addPrescription(@PathVariable Long visitId,@RequestBody MedicationPrescriptionDTO prescription) {
        return this.medicationService.addPrescription(visitId,prescription);
    }

    @PostMapping("visits/{visitId}/prescribe-treatment")
    @ResponseStatus(HttpStatus.CREATED)
    public TreatmentPrescriptionDTO prescribeTreatment(@PathVariable Long visitId, @RequestBody TreatmentPrescriptionDTO treatPreDTO){
        return this.treatmentService.createTreatment(visitId, treatPreDTO);

    }

    @GetMapping("pet/{pet_id}/incompatible/{medicationId}")
    public boolean checkIncompatibilities(@PathVariable Long pet_id, @PathVariable Long medicationId) {
        return this.medicationService.checkIncompatibilities(pet_id, medicationId);
    }

    @PutMapping("visits/{visitId}/cancel")
    public VisitDTO cancelScheduledVisit(@PathVariable Long visitId) {
        return this.visitService.cancelScheduledVisit(visitId);

    }

    @PutMapping("visits/{visitId}/noshow")
    public VisitDTO noShowScheduledVisit(@PathVariable Long visitId) {
        return this.visitService.noShowVisit(visitId);
    }

    @GetMapping("/demand/start/{start_date}/end/{end_date}")
    public List<VeterinarianDemandDTO> getVeterinarianDemand(@PathVariable LocalDate start_date, @PathVariable LocalDate end_date) {
        return this.veterinarianService.getVeterinarianDemand(start_date, end_date);
    }


}
