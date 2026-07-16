package cat.tecnocampus.application.inputDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record scheduleVisitDTO(
    Long petId,
    Long ownerId,
    Long agendaId,
    Long id,
    LocalDateTime startDate,
    int duration,
    String reason,
    int price
    )
{
}
