package cat.tecnocampus.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityDTO(
    Long availabilityId,
    int dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate periodStart,
    LocalDate periodEnd,
    List<AvailabilityExceptionDTO> availabilityExceptions
)
{}
