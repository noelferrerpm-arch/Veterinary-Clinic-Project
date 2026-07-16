package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Agenda;
import cat.tecnocampus.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgendaRepository extends JpaRepository<Agenda, Long>  {
    @Query("""
        SELECT a
        FROM Agenda a
        WHERE a.veterinarian.personId = :vetId
                """)
    List<Agenda> findByVetId(Long vetId);
}
