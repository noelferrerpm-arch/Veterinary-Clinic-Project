package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicServiceRepository extends JpaRepository<Visit, Long> {
}
