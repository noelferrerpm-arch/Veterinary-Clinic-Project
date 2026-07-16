// java
package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.LoyaltyTierDTO;
import cat.tecnocampus.application.inputDTO.LoyaltyTierCommand;
import cat.tecnocampus.application.service.LoyaltyTierService;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LoyaltyTierServiceTest {

    @Autowired
    private LoyaltyTierService loyaltyTierService;

    @Test
    public void listAll_notEmpty_whenDataSqlLoaded() {
        List<LoyaltyTierDTO> all = loyaltyTierService.listAll();
        assertNotNull(all);
        assertFalse(all.isEmpty(), "Se esperan niveles cargados por data.sql");
    }

    @Test
    public void createLoyaltyTier_and_getById() {
        LoyaltyTierCommand cmd = new LoyaltyTierCommand("SILVER", 1, 5.0, "5% discount");
        LoyaltyTierDTO created = loyaltyTierService.createLoyaltyTier(cmd);
        assertNotNull(created);
        assertNotNull(created.id());

        LoyaltyTierDTO fetched = loyaltyTierService.getLoyaltyTier(created.id());
        assertNotNull(fetched);
        assertEquals(created.id(), fetched.id());
        assertEquals("SILVER", fetched.tierName());
    }

    @Test
    public void createLoyaltyTier_duplicateRequiredPoints_throwsInvalidDataException() {
        // data.sql incluye required_points = 3 para GOLD
        LoyaltyTierCommand dupPoints = new LoyaltyTierCommand("UNIQUE_NAME", 3, 0.05, "X");
        assertThrows(InvalidDataException.class, () -> loyaltyTierService.createLoyaltyTier(dupPoints));
    }

    @Test
    public void createLoyaltyTier_duplicateName_throwsInvalidDataException() {
        // data.sql inserta un tier llamado GOLD
        LoyaltyTierCommand duplicate = new LoyaltyTierCommand("GOLD", 99, 10.0, "dup");
        assertThrows(InvalidDataException.class, () -> loyaltyTierService.createLoyaltyTier(duplicate));
    }

    @Test
    public void updateLoyaltyTier_changesFields() {
        LoyaltyTierCommand cmd = new LoyaltyTierCommand("BRONZE", 2, 2.5, "2.5% discount");
        LoyaltyTierDTO created = loyaltyTierService.createLoyaltyTier(cmd);

        LoyaltyTierDTO update = new LoyaltyTierDTO(
                created.id(),
                "BRONZE_PLUS",
                2,
                3.0,
                "3% discount upgraded"
        );

        LoyaltyTierDTO updated = loyaltyTierService.updateLoyaltyTier(created.id(), update);
        assertNotNull(updated);
        assertEquals("BRONZE_PLUS", updated.tierName());
        assertEquals(3.0, updated.discountPercentage());
    }

    @Test
    public void updateLoyaltyTier_notFound_throwsNotFoundException() {
        LoyaltyTierDTO dto = new LoyaltyTierDTO(999999L, "X", 1, 0.01, "x");
        assertThrows(NotFoundException.class, () -> loyaltyTierService.updateLoyaltyTier(999999L, dto));
    }

    @Test
    public void updateLoyaltyTier_conflictingName_throwsInvalidDataException() {
        // crear un tier temporal
        LoyaltyTierCommand cmd = new LoyaltyTierCommand("TEMP_TIER", 20, 0.02, "temp");
        LoyaltyTierDTO created = loyaltyTierService.createLoyaltyTier(cmd);

        // intentar actualizar su nombre a 'GOLD' (ja existent a data.sql)
        LoyaltyTierDTO conflict = new LoyaltyTierDTO(created.id(), "GOLD", 20, 0.02, "temp");
        assertThrows(InvalidDataException.class, () -> loyaltyTierService.updateLoyaltyTier(created.id(), conflict));

        // cleanup
        loyaltyTierService.deleteLoyaltyTier(created.id());
    }

    @Test
    public void updateLoyaltyTier_conflictingRequiredPoints_throwsInvalidDataException() {
        // crear un tier temporal
        LoyaltyTierCommand cmd = new LoyaltyTierCommand("TEMP_TIER2", 30, 0.03, "temp2");
        LoyaltyTierDTO created = loyaltyTierService.createLoyaltyTier(cmd);

        // intentar cambiar requiredPoints a 3 (valor existent a data.sql)
        LoyaltyTierDTO conflictPoints = new LoyaltyTierDTO(created.id(), created.tierName(), 3, created.discountPercentage(), created.benefitsDescription());
        assertThrows(InvalidDataException.class, () -> loyaltyTierService.updateLoyaltyTier(created.id(), conflictPoints));

        // cleanup
        loyaltyTierService.deleteLoyaltyTier(created.id());
    }

    @Test
    public void deleteLoyaltyTier_assignedToPetOwners_throwsIllegalStateException() {
        // localizar el tier 'GOLD' que en data.sql está asignado a pet owners
        Long goldId = loyaltyTierService.listAll()
                .stream()
                .filter(t -> "GOLD".equalsIgnoreCase(t.tierName()))
                .map(LoyaltyTierDTO::id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("GOLD tier no encontrado en data.sql"));

        assertThrows(IllegalStateException.class, () -> loyaltyTierService.deleteLoyaltyTier(goldId));
    }

    @Test
    public void deleteNewlyCreated_deletesSuccessfully_thenNotFound() {
        LoyaltyTierCommand cmd = new LoyaltyTierCommand("TEMP_DELETE", 999, 1.0, "temp");
        LoyaltyTierDTO created = loyaltyTierService.createLoyaltyTier(cmd);
        Long id = created.id();
        assertNotNull(id);

        loyaltyTierService.deleteLoyaltyTier(id);
        assertThrows(NotFoundException.class, () -> loyaltyTierService.getLoyaltyTier(id));
    }
}
