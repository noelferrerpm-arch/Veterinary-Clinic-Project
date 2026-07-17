package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.LoyaltyTierDTO;
import cat.tecnocampus.application.inputDTO.LoyaltyTierCommand;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.LoyaltyTier;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.LoyaltyTierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoyaltyTierService {

    private final LoyaltyTierRepository repo;

    public LoyaltyTierService(LoyaltyTierRepository repo) {
        this.repo = repo;
    }

    public LoyaltyTierDTO createLoyaltyTier(LoyaltyTierCommand loyaltyTierDTO) {
        LoyaltyTier e = new LoyaltyTier();
        e.setTierName(loyaltyTierDTO.tierName());
        e.setRequiredPoints(loyaltyTierDTO.requiredPoints());
        e.setDiscountPercentage(loyaltyTierDTO.discountPercentage());
        e.setBenefitsDescription(loyaltyTierDTO.benefitsDescription());
        if (repo.existsByTierNameIgnoreCase(e.getTierName())) {
            throw new InvalidDataException("Tier name already exists: " + e.getTierName());
        }
        if (repo.existsByRequiredPoints(e.getRequiredPoints())) {
            throw new InvalidDataException("There is already a tier with requiredPoints=" + e.getRequiredPoints());
        }
        LoyaltyTier saved = repo.save(e);
        return MapperHelper.mapLoyaltyTierDTO(saved);
    }

    public LoyaltyTierDTO updateLoyaltyTier(Long id, LoyaltyTierDTO loyaltyTierDTO) {

        LoyaltyTier existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("LoyaltyTier not found: " + id));

        int oldRequiredPoints = existing.getRequiredPoints();
        // validar duplicados
        Optional<LoyaltyTier> byName = repo.findByTierNameIgnoreCase(loyaltyTierDTO.tierName());
        if (byName.isPresent() && !byName.get().getId().equals(id)) {
            throw new InvalidDataException("Tier name already exists: " + loyaltyTierDTO.tierName());
        }

        // ahora aplicar cambios
        existing.setTierName(loyaltyTierDTO.tierName());
        existing.setDiscountPercentage(loyaltyTierDTO.discountPercentage());

        if (loyaltyTierDTO.requiredPoints() != oldRequiredPoints && repo.existsByRequiredPoints(loyaltyTierDTO.requiredPoints())) {
            throw new InvalidDataException("There is already a tier with requiredPoints=" + loyaltyTierDTO.requiredPoints());
        }
        existing.setRequiredPoints(loyaltyTierDTO.requiredPoints());

        existing.setBenefitsDescription(loyaltyTierDTO.benefitsDescription());
        LoyaltyTier saved = repo.save(existing);
        return MapperHelper.mapLoyaltyTierDTO(saved);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public LoyaltyTierDTO getLoyaltyTier(Long id) {
        LoyaltyTier e = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("LoyaltyTier not found: " + id));
        return MapperHelper.mapLoyaltyTierDTO(e);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<LoyaltyTierDTO> listAll() {
        return repo.findAll()
                .stream()
                .map(MapperHelper::mapLoyaltyTierDTO)
                .toList();
    }

    public void deleteLoyaltyTier(Long id) {
        LoyaltyTier e = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("LoyaltyTier not found: " + id));

        if (e.getPetOwners() != null && !e.getPetOwners().isEmpty()) {
            throw new IllegalStateException("Cannot delete a tier that is assigned to pet owners");
        }

        repo.delete(e);
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
