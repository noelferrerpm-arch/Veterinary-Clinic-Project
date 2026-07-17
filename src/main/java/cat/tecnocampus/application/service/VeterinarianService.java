package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.VeterinarianDTO;
import cat.tecnocampus.application.outputDTO.VeterinarianDemandDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.Status_VPR;
import cat.tecnocampus.domain.Veterinarian;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.persistence.PersonRepository;
import cat.tecnocampus.persistence.VeterinarianRepository;
import cat.tecnocampus.persistence.VisitRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VeterinarianService {
    private final VeterinarianRepository veterinarianRepository;
    private final VisitRepository visitRepository;
    private final PersonRepository personRepository;

    public VeterinarianService(VeterinarianRepository veterinarianRepository,
                               VisitRepository visitRepository, PersonRepository personRepository) {
        this.veterinarianRepository = veterinarianRepository;
        this.visitRepository = visitRepository;
        this.personRepository = personRepository;
    }

    public List<VeterinarianDTO> getAllVeterinarian() {
        return this.veterinarianRepository.findAll().stream().map(MapperHelper::mapVeterinarianDTO).toList();
    }

    public VeterinarianDTO createVeterinarian(VeterinarianDTO veterinarian) {
        Veterinarian v = new Veterinarian();
        v.setFirstName(veterinarian.firstName());
        v.setLastName(veterinarian.lastName());
        if(personRepository.existsByEmail(veterinarian.email())) {
            throw new InvalidDataException("Email already exists");
        }
        v.setEmail(veterinarian.email());
        v.setPhoneNumber(veterinarian.phoneNumber());
        v.setAddress(veterinarian.address());
        v.setLicenseNumber(veterinarian.licenseNumber());
        v.setYearsOfExperience(veterinarian.yearsOfExperience());
        this.veterinarianRepository.save(v);
        return MapperHelper.mapVeterinarianDTO(v);
    }

    public List<VeterinarianDTO> getVeterinariansAvailable() {
        List<VeterinarianDTO> veterinariansAvailable = new ArrayList<>();
        List<Veterinarian> veterinarians = this.veterinarianRepository.findAll().stream().toList();
        for (Veterinarian v : veterinarians) {
            if(v.isAvailable(LocalDateTime.now())){
                veterinariansAvailable.add(MapperHelper.mapVeterinarianDTO(v));
            }
        }
        return veterinariansAvailable;
    }

    public List<VeterinarianDemandDTO> getVeterinarianDemand(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) throw new InvalidDataException("End date must be after start date");

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23,59,59);
        Status_VPR status = Status_VPR.Scheduled;
        List<VeterinarianDemandDTO> result = visitRepository.findVeterinarianDemandBetween(startDt, endDt, status);
        return result;
    }

}
