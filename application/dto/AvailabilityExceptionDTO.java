package cat.tecnocampus.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public record AvailabilityExceptionDTO(
        Long id,
        String reason,
        int dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate periodStart,
        LocalDate periodEnd
) { }
