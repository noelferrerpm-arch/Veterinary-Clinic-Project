package cat.tecnocampus.application.dto;

public record LoyaltyTierDTO(
        Long id,
        String tierName,
        int requiredPoints,
        double discountPercentage,
        String benefitsDescription
) {}
