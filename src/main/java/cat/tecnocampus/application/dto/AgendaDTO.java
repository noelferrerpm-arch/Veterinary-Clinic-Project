package cat.tecnocampus.application.dto;

import java.time.LocalDate;
import java.util.List;

public record AgendaDTO(
        Long agendaId,
        VeterinarianDTO veterinarian,
        int agendaYear,
        List<VisitDTO> visits
) {
}
