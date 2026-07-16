package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person,Long> {
    boolean existsByEmail(String email);
}
