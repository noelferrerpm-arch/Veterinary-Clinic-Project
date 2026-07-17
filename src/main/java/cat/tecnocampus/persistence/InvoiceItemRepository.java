package cat.tecnocampus.persistence;

import cat.tecnocampus.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<Discount, Long> {
}
