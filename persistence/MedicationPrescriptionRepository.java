package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.MedicationPrescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationPrescriptionRepository extends JpaRepository<MedicationPrescription,Long> {
}
