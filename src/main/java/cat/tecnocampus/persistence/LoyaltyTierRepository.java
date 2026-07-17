package cat.tecnocampus.persistence;
import cat.tecnocampus.domain.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {
    boolean existsByTierNameIgnoreCase(String tierName);
    Optional<LoyaltyTier> findByTierNameIgnoreCase(String tierName);
    boolean existsByRequiredPoints(int requiredPoints);
}
