package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Discount;
import cat.tecnocampus.domain.PetOwner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetOwnerRepository extends JpaRepository<PetOwner, Long> {
}
