package cat.tecnocampus.api;

import cat.tecnocampus.application.dto.LoyaltyTierDTO;
import cat.tecnocampus.application.inputDTO.LoyaltyTierCommand;
import cat.tecnocampus.application.service.LoyaltyTierService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty-tiers")
public class LoyaltyTierController {

    private final LoyaltyTierService loyaltyTierService;

    public LoyaltyTierController(LoyaltyTierService loyaltyTierService) {
        this.loyaltyTierService = loyaltyTierService;
    }

    // POST
    @Operation(summary = "Create a new loyalty tier")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoyaltyTierDTO createLoyaltyTier(@Valid @RequestBody LoyaltyTierCommand body) {
        return loyaltyTierService.createLoyaltyTier(body);
    }

    // GET all
    @Operation(summary = "Get all loyalty tiers")
    @GetMapping
    public List<LoyaltyTierDTO> getAllLoyaltyTiers() {
        return loyaltyTierService.listAll();
    }

    // GET by id
    @Operation(summary = "Get loyalty tier by id")
    @GetMapping("/{id}")
    public LoyaltyTierDTO getLoyaltyTier(@PathVariable Long id) {
        return loyaltyTierService.getLoyaltyTier(id);
    }

    // PUT
    @Operation(summary = "Update loyalty tier by id")
    @PutMapping("/{id}")
    public LoyaltyTierDTO updateLoyaltyTier(@PathVariable Long id, @Valid @RequestBody LoyaltyTierDTO body) {
        return loyaltyTierService.updateLoyaltyTier(id, body);
    }

    // DELETE
    @Operation(summary = "Delete loyalty tier by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLoyaltyTier(@PathVariable Long id) {
        loyaltyTierService.deleteLoyaltyTier(id);
    }
}
