package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {
}
