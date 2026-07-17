package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.MedicationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MedicationBatchRepository extends JpaRepository<MedicationBatch, Long> {

    @Query("""
            SELECT a
            FROM MedicationBatch a
            WHERE a.medicationId = :id
            """)
    List<MedicationBatch> findallMedicationsById (Long id);

}
