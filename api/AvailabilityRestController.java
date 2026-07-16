package cat.tecnocampus.api;

import cat.tecnocampus.application.dto.AvailabilityDTO;
import cat.tecnocampus.application.dto.AvailabilityExceptionDTO;
import cat.tecnocampus.application.service.AvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarian/{veterinarianId}/availability")
public class AvailabilityRestController {

    private final AvailabilityService availabilityService;

    public AvailabilityRestController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping()
    public List<AvailabilityDTO> getAvailabilities(@PathVariable Long veterinarianId) {
        return this.availabilityService.getAvailabilities(veterinarianId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityDTO createAvailability(@PathVariable Long veterinarianId, @RequestBody AvailabilityDTO availabilityDTO) {
        return this.availabilityService.createAvailability(veterinarianId, availabilityDTO);
    }

    @PostMapping("/{availabilityId}/exception/")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityExceptionDTO createAvailabilityException(@PathVariable Long veterinarianId, @PathVariable Long availabilityId, @RequestBody AvailabilityExceptionDTO availabilityExceptionDTO) {
        return this.availabilityService.createAvailabilityException(veterinarianId, availabilityId, availabilityExceptionDTO);
    }

    @PutMapping("/{availabilityId}")
    public AvailabilityDTO updateAvailability(@PathVariable Long veterinarianId, @PathVariable Long availabilityId, @RequestBody AvailabilityDTO availabilityDTO) {
        return this.availabilityService.updateAvailability(veterinarianId, availabilityId, availabilityDTO);
    }


    @DeleteMapping("/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(@PathVariable Long veterinarianId, @PathVariable Long availabilityId) {
        this.availabilityService.deleteAvailability(veterinarianId, availabilityId);
    }

    @DeleteMapping("/{availabilityId}/exception/{availabilityExceptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailabilityException(@PathVariable Long veterinarianId, @PathVariable Long availabilityId, @PathVariable Long availabilityExceptionId) {
        this.availabilityService.deleteAvailabilityException(veterinarianId, availabilityId, availabilityExceptionId);
    }


}
