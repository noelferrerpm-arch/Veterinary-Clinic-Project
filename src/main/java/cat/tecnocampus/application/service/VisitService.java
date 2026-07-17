package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.AgendaDTO;
import cat.tecnocampus.application.dto.VisitDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.*;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.*;
import cat.tecnocampus.application.inputDTO.scheduleVisitDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VisitService {

    public VisitRepository visitRepository;
    public VeterinarianRepository veterinarianRepository;
    public AgendaRepository agendaRepository;
    public PetOwnerRepository petOwnerRepository;
    public PetRepository petRepository;

    public VisitService(VisitRepository visitRepository,
                        AgendaRepository agendaRepository,
                        VeterinarianRepository veterinarianRepository,
                        PetRepository petRepository,
                        PetOwnerRepository petOwnerRepository) {
        this.visitRepository = visitRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.agendaRepository = agendaRepository;
        this.petRepository=petRepository;
        this.petOwnerRepository=petOwnerRepository;
    }

    public VisitDTO createVisit(scheduleVisitDTO visitDTO) {
        Pet pet = petRepository.getReferenceById(visitDTO.petId());
        PetOwner owner=petOwnerRepository.getReferenceById(visitDTO.ownerId());
        Agenda agenda=agendaRepository.getReferenceById(visitDTO.agendaId());
        Veterinarian vet=agenda.getVeterinarian();
        LocalDateTime schedule = visitDTO.startDate();
        boolean available=vet.isAvailable(schedule);
        if(!available){
            throw new InvalidDataException("Veterinarian is not available");
        }
        Visit visit = new Visit();
        visit.setDuration(visitDTO.duration());
        visit.setPrice(visitDTO.price());
        visit.setReason(visitDTO.reason());
        visit.setStartDate(visitDTO.startDate());
        visit.setStatus(Status_VPR.Scheduled);
        visit.setOwner(owner);
        visit.setPet(pet);
        visit.setAgenda(agenda);
        visit.setVeterinarian(vet);
        agenda.addScheduledVisit(visit);
        this.visitRepository.save(visit);

        return MapperHelper.mapVisitDTO(visit);
    }

    public VisitDTO createVisitWalkIn(Long veterinarianId, scheduleVisitDTO visitDTO) {
        Pet pet = petRepository.findById(visitDTO.petId()).orElseThrow(()-> new NotFoundException("Pet not found"));
        PetOwner owner=petOwnerRepository.findById(visitDTO.ownerId()).orElseThrow(()-> new NotFoundException("Pet Owner not found"));
        Agenda agenda=agendaRepository.getReferenceById(visitDTO.agendaId());

        LocalDateTime schedule = LocalDateTime.now();
        Veterinarian vet = this.veterinarianRepository.findById(veterinarianId).orElseThrow(()-> new NotFoundException("Veterinarian not found"));
        boolean available=vet.isAvailable(schedule);
        if(!available){
            throw new InvalidDataException("There is no veterinarian available");
        }
        Visit visit = new Visit();
        visit.setDuration(visitDTO.duration());
        visit.setPrice(visitDTO.price());
        visit.setReason(visitDTO.reason());
        visit.setStartDate(LocalDateTime.now());
        visit.setStatus(Status_VPR.In_Progress);
        visit.setOwner(owner);
        visit.setPet(pet);
        visit.setAgenda(agenda);
        visit.setVeterinarian(vet);
        this.visitRepository.save(visit);

        return MapperHelper.mapVisitDTO(visit);
    }

    public void deleteVisit(Long visitId){
        Visit visit= this.visitRepository.findById(visitId).orElseThrow(()-> new NotFoundException("Visit not found"));
        this.visitRepository.delete(visit);
    }

    public VisitDTO reescheduleVisit(Long id, scheduleVisitDTO visitDTO) {
        Visit visit=this.visitRepository.findById(id).orElseThrow(()-> new NotFoundException("Visit not found"));
        if(visit.getStatus()!=Status_VPR.Scheduled){
            throw new InvalidDataException("Visit is not scheduled");
        }
        Agenda agenda=agendaRepository.getReferenceById(visitDTO.agendaId());
        Veterinarian vet=agenda.getVeterinarian();
        LocalDateTime schedule = visitDTO.startDate();
        boolean available=vet.isAvailable(schedule);
        if(!available){
            throw new InvalidDataException("Veterinarian is not available");
        }
        agenda.removeScheduledVisit(visit);
        visit.setStartDate(visitDTO.startDate());
        agenda.addScheduledVisit(visit);
        return MapperHelper.mapVisitDTO(visit);
    }

    public VisitDTO cancelScheduledVisit(Long visitId) {
        Visit visit=this.visitRepository.findById(visitId).orElseThrow(()-> new NotFoundException("Visit not found"));
        if(visit.getStatus()!=Status_VPR.Scheduled){
            throw new InvalidDataException("Visit is not scheduled");
        }
        Agenda agenda= visit.getAgenda();
        agenda.removeScheduledVisit(visit);

        visit.setStatus(Status_VPR.Cancelled);
        return MapperHelper.mapVisitDTO(visit);

    }

    public VisitDTO getVisit(Long visitId){
        Visit visit=this.visitRepository.findById(visitId).orElseThrow(()-> new NotFoundException("Visit not found"));
        return MapperHelper.mapVisitDTO(visit);
    }

    public List<VisitDTO> getAllVisits(){
        return this.visitRepository.findAll().stream().map(MapperHelper::mapVisitDTO).toList();
    }


    public List<VisitDTO> getVisitsBetweenDates(Long vetId, LocalDate start, LocalDate end){
        if (end.isBefore(start)){
            throw new InvalidDataException("End date must be after start date");
        }
        Veterinarian vet=this.veterinarianRepository.findById(vetId).orElseThrow(()-> new NotFoundException("Veterinarian not found"));

        List<VisitDTO> allVisits=this.visitRepository.findAll().stream().map(MapperHelper::mapVisitDTO).toList();
        List<VisitDTO> visits= new ArrayList<VisitDTO>();
        for(VisitDTO visit : allVisits){
            if(visit.startDate().isBefore(end.atTime(23,59,59))&&visit.startDate().isAfter(start.atStartOfDay()) && visit.vetId().equals(vetId)){
                visits.add(visit);
            }
        }

        return visits;
    }

    public List<AgendaDTO> getAgendaByVetId(Long vet_id) {
        List<AgendaDTO> agendas = this.agendaRepository.findByVetId(vet_id)
                .stream()
                .map(MapperHelper::mapAgendaDTO)
                .toList();

        if (agendas.isEmpty()) {
            throw new NotFoundException("No agendas found for veterinarian id: " + vet_id);
        }

        return agendas;
    }


    public VisitDTO initiateVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId).orElseThrow();
        visit.initiateVisit();
        return MapperHelper.mapVisitDTO(visit);
    }

    public VisitDTO completeVisit(Long visitId) {
        Visit visit = visitRepository.findById(visitId).orElseThrow();
        visit.completeVisit();
        return MapperHelper.mapVisitDTO(visit);
    }

    public VisitDTO noShowVisit(Long visitId) {
        Visit visit=this.visitRepository.findById(visitId).orElseThrow(()-> new NotFoundException("Visit not found"));
        if(visit.getStatus()!=Status_VPR.Scheduled){
            throw new InvalidDataException("Visit is not scheduled");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = visit.getStartDate().plusMinutes(10);
        if (now.isBefore(threshold)) {
            throw new InvalidDataException(
                    "Cannot mark visit as Not Showed Up before 10 minutes after the scheduled start time ("
                            + threshold + "). Current time: " + now);
        }
        visit.setStatus(Status_VPR.Not_Showed_Up);
        return MapperHelper.mapVisitDTO(visit);
    }

    public VisitDTO addDiagnosis(Long visitId, Map<String, String> diagnosis) {
        Visit visit = visitRepository.findById(visitId).orElseThrow(() -> new NotFoundException("Visit not found"));
        if(visit.getStatus() != Status_VPR.In_Progress && visit.getStatus() != Status_VPR.Completed){
            throw new InvalidDataException("Visit is not in progress or completed");
        }
        diagnosis.forEach((key, value) -> {
            switch (key) {
                case "diagnosis":
                    visit.setDiagnosis(value);
                    break;
                case "notes":
                    visit.setNotes(value);
                    break;
            }
        });
        return MapperHelper.mapVisitDTO(visit);
    }

}
