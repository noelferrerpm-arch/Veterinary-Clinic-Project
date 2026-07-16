package cat.tecnocampus.persistence;
import cat.tecnocampus.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PetRepository extends JpaRepository<Pet,Long>{
}
