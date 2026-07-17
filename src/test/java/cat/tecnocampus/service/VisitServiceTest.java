package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.AgendaDTO;
import cat.tecnocampus.application.dto.VisitDTO;
import cat.tecnocampus.application.inputDTO.scheduleVisitDTO;
import cat.tecnocampus.application.service.VisitService;
import cat.tecnocampus.domain.Status_VPR;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VisitServiceTest {

    @Autowired
    private VisitService visitService;

    @Test
    public void getAllVisits_notEmpty_whenDataSqlLoaded() {
        var all = visitService.getAllVisits();
        assertNotNull(all);
        assertFalse(all.isEmpty(), "Se esperan visitas cargadas por data.sql");
    }

    @Test
    public void create_visit_programmed_get_and_delete_flow() {
        List<AgendaDTO> agendas = visitService.getAgendaByVetId(1L);
        long agendaId = agendas.get(0).agendaId();

        LocalDateTime scheduled = LocalDateTime.of(2025, 11, 20, 10, 0);
        scheduleVisitDTO dto = new scheduleVisitDTO(1L, 2L, agendaId, 1L, scheduled, 20, "Chequeo programado", 30);

        VisitDTO created = visitService.createVisit(dto);
        assertNotNull(created);
        assertEquals(Status_VPR.Scheduled, created.status());

        VisitDTO fetched = visitService.getVisit(created.visitId());
        assertEquals(created.visitId(), fetched.visitId());

        visitService.deleteVisit(created.visitId());
        assertThrows(NotFoundException.class, () -> visitService.getVisit(created.visitId()));
    }

    @Test
    public void create_walkin_initiate_and_complete_flow() {
        // walk-in uses veterinarianId param, petId and ownerId must exist (data.sql: pet 1 owner 2)
        scheduleVisitDTO dto = new scheduleVisitDTO(1L, 2L, 3L, 1L, LocalDateTime.now(), 30,"WalkIn", 20);
        VisitDTO walkIn = visitService.createVisitWalkIn(1L, dto);
        assertNotNull(walkIn);
        assertEquals(Status_VPR.In_Progress, walkIn.status());

        VisitDTO completed = visitService.completeVisit(walkIn.visitId());
        assertEquals(Status_VPR.Completed, completed.status());

        // cleanup
        visitService.deleteVisit(walkIn.visitId());
    }

    @Test
    public void reeschedule_cancel_and_noShow_flow() {
        List<AgendaDTO> agendas = visitService.getAgendaByVetId(1L);
        long agendaId = agendas.get(0).agendaId();

        // create scheduled visit in the past beyond 10 minutes to allow noShow
        LocalDateTime past = LocalDateTime.now().minusMinutes(20);
        scheduleVisitDTO dto = new scheduleVisitDTO(1L, 2L, agendaId, 1L, past, 10, "Para noShow", 15);
        VisitDTO created = visitService.createVisit(dto);
        long id = created.visitId();

        // reeschedule to another slot (must be Scheduled to reeschedule, so create new scheduled visit)
        LocalDateTime newStart = LocalDateTime.now().plusDays(1);
        scheduleVisitDTO reDto = new scheduleVisitDTO(1L, 2L, agendaId, 2L, newStart, 10, "Reprogramado", 40);
        VisitDTO re = visitService.reescheduleVisit(id, reDto);
        assertEquals(newStart.withSecond(0).withNano(0).getDayOfMonth(), re.startDate().getDayOfMonth());

        // cancel scheduled visit
        VisitDTO cancelled = visitService.cancelScheduledVisit(id);
        assertEquals(Status_VPR.Cancelled, cancelled.status());

        // create another scheduled visit in past to test noShow
        scheduleVisitDTO dto2 = new scheduleVisitDTO(1L, 2L, agendaId, 1L, LocalDateTime.now().minusMinutes(20), 15,"Para noShow2", 15);
        VisitDTO v2 = visitService.createVisit(dto2);
        VisitDTO noShow = visitService.noShowVisit(v2.visitId());
        assertEquals(Status_VPR.Not_Showed_Up, noShow.status());

        // cleanup
        visitService.deleteVisit(id);
        visitService.deleteVisit(v2.visitId());
    }

    @Test
    public void getVisitsBetweenDates_and_invalidRange() {
        LocalDate start = LocalDate.of(2024, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 2);
        var visits = visitService.getVisitsBetweenDates(1L, start, end);
        assertNotNull(visits);

        // invalid range
        assertThrows(InvalidDataException.class, () -> visitService.getVisitsBetweenDates(1L, end, start));
    }

    @Test
    public void getAgendaByVetId_returns_list_or_throws() {
        var agendas = visitService.getAgendaByVetId(1L);
        assertNotNull(agendas);
        assertFalse(agendas.isEmpty());

        // id without agendas should throw NotFoundException (assumes no vet with id 9999)
        assertThrows(NotFoundException.class, () -> visitService.getAgendaByVetId(9999L));
    }

    @Test
    public void addDiagnosis_requires_in_progress_or_completed() {
        // create walk-in which is In_Progress
        scheduleVisitDTO dto = new scheduleVisitDTO(1L, 2L, 3L, 1L, LocalDateTime.now(), 10, "WalkInDiag", 20);
        VisitDTO walkIn = visitService.createVisitWalkIn(1L, dto);
        long id = walkIn.visitId();

        Map<String, String> diag = new HashMap<>();
        diag.put("diagnosis", "Gripe");
        diag.put("notes", "Observaciones");

        VisitDTO updated = visitService.addDiagnosis(id, diag);
        assertEquals("Gripe", updated.diagnosis());
        assertEquals("Observaciones", updated.notes());

        // cleanup
        visitService.deleteVisit(id);
    }

    @Test
    public void reeschedule_or_cancel_on_non_scheduled_throws() {
        // create walk-in -> status In_Progress
        scheduleVisitDTO dto = new scheduleVisitDTO(1L, 2L, 3L, 1L, LocalDateTime.now(), 10, "WalkInReEschedule", 20);
        VisitDTO walkIn = visitService.createVisitWalkIn(1L, dto);
        long id = walkIn.visitId();

        // reeschedule should fail because not Scheduled
        scheduleVisitDTO reDto = new scheduleVisitDTO(1L, 2L, 1L, 1L, LocalDateTime.now().plusDays(1), 10,"WalkInReEschedule", 20);
        assertThrows(InvalidDataException.class, () -> visitService.reescheduleVisit(id, reDto));

        // cancel should fail because not Scheduled
        assertThrows(InvalidDataException.class, () -> visitService.cancelScheduledVisit(id));

        // cleanup
        visitService.deleteVisit(id);
    }
}
