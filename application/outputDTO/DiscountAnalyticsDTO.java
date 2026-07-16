package cat.tecnocampus.application.outputDTO;

import java.time.LocalDate;

public record DiscountAnalyticsDTO(
        Long discountId,
        String discountCode,
        String discountType,
        Double discountValue,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxUses,
        Integer usesCount,
        Long timesUsed,
        Long uniqueCustomersUsed,
        Double totalDiscountAmount,
        Double totalFinalRevenue,
        Double totalOriginalRevenue,
        Double discountImpactPercentage,
        Double usageRatePercentage,
        Double avgDiscountPerInvoice,
        LocalDate firstUsedDate,
        LocalDate lastUsedDate
) {
}
