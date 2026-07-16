package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

//    @Query("""
//            SELECT m
//            FROM Medication m
//            WHERE m.medicationId = :id
//            """)
//    Optional<Medication> findById(long id);
}
