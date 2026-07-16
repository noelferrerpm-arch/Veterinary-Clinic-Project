package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.AvailabilityDTO;
import cat.tecnocampus.application.dto.AvailabilityExceptionDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.Availability;
import cat.tecnocampus.domain.AvailabilityException;
import cat.tecnocampus.domain.Veterinarian;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.VeterinarianRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class AvailabilityService {

    private final VeterinarianRepository veterinarianRepository;

    public AvailabilityService(VeterinarianRepository veterinarianRepository) {
        this.veterinarianRepository = veterinarianRepository;
    }

    public List<AvailabilityDTO> getAvailabilities(Long veterinarianId) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(()-> new NotFoundException("Veterinarian not found"));
        List<Availability> availabilities = veterinarian.getAvailability();
        return availabilities.stream().map(MapperHelper::mapAvailabilityDTO).toList();
    }

    public AvailabilityDTO getAvailabilityById(Long veterinarianId, Long availabilityId) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(()-> new NotFoundException("Veterinarian not found"));
        Availability availability = veterinarian.getAvailabilityById(availabilityId);
        return MapperHelper.mapAvailabilityDTO(availability);
    }

    public AvailabilityDTO createAvailability(Long veterinarianId, AvailabilityDTO availabilityDTO) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(()-> new NotFoundException("Veterinarian not found"));
        Availability availability = new Availability();
        availability.setDayOfWeek(availabilityDTO.dayOfWeek());
        availability.setStartTime(availabilityDTO.startTime());
        availability.setEndTime(availabilityDTO.endTime());
        availability.setPeriodStart(availabilityDTO.periodStart());
        availability.setPeriodEnd(availabilityDTO.periodEnd());
        availability.setVeterinarian(veterinarian);
        veterinarian.addAvailability(availability);
        veterinarianRepository.save(veterinarian);
        return MapperHelper.mapAvailabilityDTO(availability);
    }

    public AvailabilityExceptionDTO createAvailabilityException(Long veterinarianId, Long availabilityId, AvailabilityExceptionDTO availabilityExceptionDTO) throws  NotFoundException {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(NotFoundException::new);
        Availability availability = veterinarian.getAvailabilityById(availabilityId);
        AvailabilityException availabilityException = new AvailabilityException();
        availabilityException.setReason(availabilityExceptionDTO.reason());
        availabilityException.setDayOfWeek(availabilityExceptionDTO.dayOfWeek());
        availabilityException.setStartTime(availabilityExceptionDTO.startTime());
        availabilityException.setEndTime(availabilityExceptionDTO.endTime());
        availabilityException.setPeriodStart(availabilityExceptionDTO.periodStart());
        availabilityException.setPeriodEnd(availabilityExceptionDTO.periodEnd());
        availabilityException.setAvailability(availability);
        availability.addAvailabilityException(availabilityException);
        return MapperHelper.mapAvailabilityExceptionDTO(availabilityException);
    }

    public AvailabilityDTO updateAvailability(Long veterinarianId, Long availabilityId, AvailabilityDTO availabilityDTO) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(NotFoundException::new);
        Availability availability = veterinarian.updateAvailability(availabilityId, availabilityDTO);
        return MapperHelper.mapAvailabilityDTO(availability);
    }

    public void deleteAvailability(Long veterinarianId, Long availabilityId) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(NotFoundException::new);
        veterinarian.removeAvailability(availabilityId);
    }

    public void deleteAvailabilityException(Long veterinarianId, Long availabilityId, Long availabilityExceptionId) {
        Veterinarian veterinarian = veterinarianRepository.findById(veterinarianId).orElseThrow(NotFoundException::new);
        Availability availability = veterinarian.getAvailabilityById(availabilityId);
        availability.removeAvailabilityException(availabilityExceptionId);
    }
}
