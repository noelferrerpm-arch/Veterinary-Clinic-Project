package cat.tecnocampus.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import cat.tecnocampus.domain.Treatment;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {


}
