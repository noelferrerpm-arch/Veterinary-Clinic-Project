package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.*;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.*;
import cat.tecnocampus.domain.exceptions.IncompatibleMedicationException;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.domain.exceptions.belowThresholdMedicationException;
import cat.tecnocampus.persistence.*;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class MedicationService {
    private final VisitRepository visitRepository;
    private final PetRepository petRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationBatchRepository medicationBatchRepository;
    private final MedicationPrescriptionRepository medicationPrescriptionRepository;

    private boolean downThreshold=true;
    public MedicationService(MedicationRepository medicationRepository, MedicationBatchRepository medicationBatchRepository, MedicationPrescriptionRepository medicationPrescriptionRepository, VisitRepository visitRepository, PetRepository petRepository) {
        this.medicationRepository = medicationRepository;
        this.medicationBatchRepository = medicationBatchRepository;
        this.medicationPrescriptionRepository =medicationPrescriptionRepository;
        this.visitRepository = visitRepository;
        this.petRepository = petRepository;
    }

    public MedicationBatchDTO arriveNewMedicationBatch(MedicationBatchDTO medicationB, Long medicationID){
        // ID check
        MedicationBatch mb=new MedicationBatch();
        Medication medication = medicationRepository.findById(medicationID).orElseThrow(()-> new NotFoundException("Medication not found"));
        mb.setMedicationId(medicationID);
        mb.setLotNumber(medicationB.lotNumber());
        mb.setReceivedDate(medicationB.receivedDate());
        mb.setExpiryDate(medicationB.expiryDate());
        mb.setInitialQuantity(medicationB.initialQuantity());
        mb.setCurrentQuantity(medicationB.initialQuantity());
        mb.setPurchagePricePerUnit(medicationB.purchagePricePerUnit());
        mb.setStorageLocation(medicationB.storageLocation());
        mb.setReorderThreshold(medicationB.reorderThreshold());
        medicationBatchRepository.save(mb);
        return MapperHelper.mapMedicationBatchDTO(mb);
    }

    @Transactional
    public MedicationBatchDTO deleteMedicationBatch(Long id){
        MedicationBatch mb=medicationBatchRepository.findById(id).orElseThrow(()-> new NotFoundException("Medication batch not found"));
        medicationBatchRepository.delete(mb);
        return MapperHelper.mapMedicationBatchDTO(mb);
    }

    public void restockAdvice(MedicationBatch medicationBatch){

        if(medicationBatch.getCurrentQuantity()<medicationBatch.getReorderThreshold()){
            //Alerta baix stock
            if(downThreshold) {
                downThreshold=false;
                throw new belowThresholdMedicationException(medicationRepository.findById(medicationBatch.getMedicationId()).get());
            }
        }else{
            downThreshold=true;
        }

    }

    public MedicationPrescriptionDTO addPrescription(Long visitID,MedicationPrescriptionDTO medPreDTO) {
        Visit v=visitRepository.findById(visitID).orElseThrow(()-> new NotFoundException("Visit not found"));
        if(v.getStatus()!=Status_VPR.In_Progress&&v.getStatus()!=Status_VPR.Completed){
            throw new InvalidDataException("No Visit in progress or completed");
        }
        MedicationPrescription medicationPrescription = new MedicationPrescription();//new MedicationPrescription(medPreDTO.prescriptionId(),medPreDTO.quantityPrescribed(),medPreDTO.dosageInstructions(),medPreDTO.duration(),medPreDTO.visit(),medPreDTO.medication());
        medicationPrescription.setQuantityPrescribed(medPreDTO.quantityPrescribed());
        medicationPrescription.setDosageInstructions(medPreDTO.dosageInstructions());
        medicationPrescription.setDuration(medPreDTO.duration());
        medicationPrescription.setVisit(v);
       Medication m= medicationRepository.findById(medPreDTO.medicationId()).orElseThrow(()-> new NotFoundException("Medication not found"));
        if(!m.checkIncompatibilities(v.getPet())){
            throw new IncompatibleMedicationException("Medication: "+medicationPrescription.getMedication().getName()+" is incompatible with "+v.getPet().getName()+" prescriptions");
        }
        medicationPrescription.setMedication(m);
        MedicationBatch toGive=null;
        for(MedicationBatch mb: medicationBatchRepository.findallMedicationsById(medPreDTO.medicationId())){
            toGive=mb.betterOption(toGive, medicationPrescription.getQuantityPrescribed());
        }
        if(toGive==null){
            throw new InvalidDataException("Not enought medication in any batch");
        }

        if(v.getPet().getPrescriptions().contains(medicationPrescription)){
            throw new InvalidDataException("Prescription already exists for this pet");
        }
        toGive.setCurrentQuantity(toGive.getCurrentQuantity()-medPreDTO.quantityPrescribed());
        restockAdvice(toGive);
        medicationPrescription.setMedication(m);
        medicationBatchRepository.save(toGive);

        medicationPrescriptionRepository.save(medicationPrescription);
        v.getPet().addPrescription(medicationPrescription);
        return MapperHelper.mapMedicationPrescriptionDTO(medicationPrescription);
    }

    public MedicationDTO createMedication(MedicationDTO medicationDTO){
        Medication medication = new Medication();
        medication.setName(medicationDTO.name());
        medication.setActiveIngredient(medicationDTO.activeIngredient());
        medication.setDosageUnit(medicationDTO.dosageUnit());
        medication.setUnitPrice(medicationDTO.unitPrice());

        medicationRepository.save(medication);
        return MapperHelper.mapMedicationDTO(medication);
    }

    public MedicationDTO sellMedication(Long medication, int quantity){
        MedicationBatch toGive=null;
        for(MedicationBatch mb: medicationBatchRepository.findallMedicationsById(medication)){
            toGive=mb.betterOption(toGive, quantity);
        }
        if(toGive==null){
            throw new InvalidDataException("Not enought medication in any batch");
        }
        toGive.setCurrentQuantity(toGive.getCurrentQuantity()-quantity);
        restockAdvice(toGive);
        medicationBatchRepository.save(toGive);

        return MapperHelper.mapMedicationDTO(
                medicationRepository.findById(medication).orElseThrow(NotFoundException::new)
        );
    }

    public MedicationDTO addIncompatibility(Long medicationId, Long incompatibilityId){
        Medication med =medicationRepository.findById(medicationId).orElseThrow(NotFoundException::new);
        Medication inco=medicationRepository.findById(incompatibilityId).orElseThrow(NotFoundException::new);
        med.addIncompatibilities(inco);
        inco.addIncompatibilities(med);
        medicationRepository.save(med);
        medicationRepository.save(inco);
        return MapperHelper.mapMedicationDTO(med);

    }

    public MedicationDTO removeIncompatibility(Long medicationId, Long incompatibilityId){
        Medication med =medicationRepository.findById(medicationId).orElseThrow(NotFoundException::new);
        Medication inco=medicationRepository.findById(incompatibilityId).orElseThrow(NotFoundException::new);
        med.removeIncompatibilities(inco);
        inco.removeIncompatibilities(med);
        medicationRepository.save(med);
        medicationRepository.save(inco);
        return MapperHelper.mapMedicationDTO(med);
    }

    public boolean checkIncompatibilities(Long petId, Long medicationId){
        return medicationRepository.findById(medicationId).orElseThrow(NotFoundException::new).checkIncompatibilities(petRepository.findById(petId).orElseThrow(NotFoundException::new));
    }


    public List<MedicationDTO> getAllMedications(){
        return this.medicationRepository.findAll().stream().map(MapperHelper::mapMedicationDTO).toList();
    }

    public MedicationDTO getById(Long id){
        return MapperHelper.mapMedicationDTO(this.medicationRepository.findById(id).orElseThrow(()-> new NotFoundException("Medication not found")));
    }


    public List<MedicationBatchDTO> getBatchesOfMedication(Long id){
        List<MedicationBatchDTO> batches= new ArrayList<>();
        for(MedicationBatch med : medicationBatchRepository.findallMedicationsById(id)){
            batches.add(MapperHelper.mapMedicationBatchDTO(med));
        }
        return batches;
    }

    public List<MedicationDTO> getMedsBetweenDates(LocalDate start, LocalDate end){
        if (end.isBefore(start)) {
            throw new InvalidDataException("End date must be after start date");
        }

        Map<Long, Long> medicationCounts = this.medicationBatchRepository.findAll().stream()
                .map(MapperHelper::mapMedicationBatchDTO)
                .filter(b -> !b.receivedDate().isBefore(ChronoLocalDate.from(start.atStartOfDay())) &&
                        !b.receivedDate().isAfter(ChronoLocalDate.from(end.atTime(23, 59, 59))))
                .collect(Collectors.groupingBy(
                        MedicationBatchDTO::medicationId,
                        Collectors.counting()
                ));
        List<Long> sortedMedicationIds = medicationCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        return sortedMedicationIds.stream()
                .map(id -> MapperHelper.mapMedicationDTO(Objects.requireNonNull(this.medicationRepository.findById(id).orElse(null))))
                .toList();
    }

    public List<VeterinarianDTO> getMedsBetweenDatesOfVet(Long medicationId, LocalDate start, LocalDate end){
        if (end.isBefore(start)) {
            throw new InvalidDataException("End date must be after start date");
        }
        Medication searching=medicationRepository.findById(medicationId).orElseThrow(()-> new NotFoundException("Medication not found"));


        List<Veterinarian> havePrescribed=new ArrayList<>();
        for(MedicationPrescription mp: medicationPrescriptionRepository.findAll()){
            if(mp.getMedication().equals(searching)&& mp.getVisit().getStartDate().isAfter(start.atStartOfDay())&&mp.getVisit().getStartDate().isBefore(end.atStartOfDay())){
                havePrescribed.add(mp.getVisit().getVeterinarian());
            }
        }

        Map<Veterinarian, Long> frecuencia = havePrescribed.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        List<Veterinarian> soloElementos = frecuencia.entrySet().stream()
                .sorted(Map.Entry.<Veterinarian, Long>comparingByValue())
                .map(Map.Entry::getKey)
                .toList();


        List<VeterinarianDTO> veterinarians = new ArrayList<>();
        for(Veterinarian v : soloElementos){
            veterinarians.add(MapperHelper.mapVeterinarianDTO(v));
        }

        return veterinarians;

    }














}



