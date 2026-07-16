package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.PetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetTypeRepository  extends JpaRepository<PetType, Long> {
}
