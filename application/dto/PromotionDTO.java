package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.Discount;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.util.List;

public record PromotionDTO(
        Long promotionId,
        String name,
        String description,
        String discountCode,
        LocalDate startDate,
        LocalDate endDate,
        List<DiscountDTO> discounts
) {
}
