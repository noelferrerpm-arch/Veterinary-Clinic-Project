package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.DiscountType;

import java.time.LocalDate;

public record DiscountDTO(
        String code,
        DiscountType type,
        double discountValue,
        LocalDate startDate,
        LocalDate endDate,
        int maxUses,
        int usesCount
) {
}
