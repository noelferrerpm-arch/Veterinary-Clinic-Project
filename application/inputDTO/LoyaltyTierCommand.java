package cat.tecnocampus.application.inputDTO;

import jakarta.validation.constraints.*;


public record LoyaltyTierCommand(
        @Pattern(regexp="^.{1,50}$") String tierName,
        @NotNull @Min(0) Integer requiredPoints,
        @NotNull @DecimalMin(value = "0.00") @DecimalMax(value = "100.00") @Digits(integer = 3, fraction = 2) Double discountPercentage,
        @Pattern(regexp="^.{0,255}$") String benefitsDescription

) {
}