package cat.tecnocampus.application.mapper;
import cat.tecnocampus.application.dto.PromotionDTO;
import cat.tecnocampus.application.outputDTO.DiscountAnalyticsDTO;
import cat.tecnocampus.domain.Discount;
import cat.tecnocampus.domain.Promotion;
import cat.tecnocampus.application.dto.DiscountDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
public class PromotionDiscountMapper {
    public static PromotionDTO mapToDTO(Promotion promotion) {
        List<DiscountDTO> discountDTOs = promotion.getDiscounts().stream()
                .map(discount -> new DiscountDTO(discount.getCode(), discount.getType(), discount.getDiscountValue(), discount.getStartDate(), discount.getEndDate(), discount.getMaxUses(), discount.getUsesCount()))
                .toList();

        return new PromotionDTO(
                promotion.getPromotionId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getDiscountCode(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                discountDTOs
        );
    }

    public static DiscountDTO mapToDTO(Discount discount) {
        return new DiscountDTO(
                discount.getCode(),
                discount.getType(),
                discount.getDiscountValue(),
                discount.getStartDate(),
                discount.getEndDate(),
                discount.getMaxUses(),
                discount.getUsesCount()
        );
    }

    public static List<Discount> mapDiscounts(List<DiscountDTO> discountDTOs) {
        List<Discount> discounts = discountDTOs.stream().map(discountDTO -> {;
            Discount discount = new Discount();
            discount.setCode(discountDTO.code());
            discount.setType(discountDTO.type());
            discount.setDiscountValue(discountDTO.discountValue());
            discount.setStartDate(discountDTO.startDate());
            discount.setEndDate(discountDTO.endDate());
            discount.setMaxUses(discountDTO.maxUses());
            discount.setUsesCount(discountDTO.usesCount());
            return discount;
        }).toList();
        return discounts;

    }

    public static List<DiscountAnalyticsDTO> mapDataToDTO(List<Object[]> rawData) {
        return rawData.stream()
                .map(PromotionDiscountMapper::mapRowToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public static DiscountAnalyticsDTO mapRowToDTO(Object[] row) {
        try {
            return new DiscountAnalyticsDTO(
                    safeLong(row[0]),           // discountId
                    safeString(row[1]),         // discountCode
                    safeString(row[2]),         // discountType
                    safeDouble(row[3]),         // discountValue
                    safeLocalDate(row[4]),      // startDate
                    safeLocalDate(row[5]),      // endDate
                    safeInteger(row[6]),        // maxUses (can be null)
                    safeInteger(row[7]),        // usesCount
                    safeLong(row[8]),           // timesUsed
                    safeLong(row[9]),           // uniqueCustomersUsed
                    safeDouble(row[10]),        // totalDiscountAmount
                    safeDouble(row[11]),        // totalFinalRevenue
                    safeDouble(row[12]),        // totalOriginalRevenue
                    safeDouble(row[13]),        // discountImpactPercentage
                    safeDouble(row[14]),        // usageRatePercentage
                    safeDouble(row[15]),        // avgDiscountPerInvoice
                    safeLocalDate(row[16]),     // firstUsedDate (can be null)
                    safeLocalDate(row[17])      // lastUsedDate (can be null)
            );
        } catch (Exception e) {
            System.err.println("Error mapping row: " + Arrays.toString(row));
            throw new RuntimeException("Failed to map analytics data: " + e.getMessage(), e);
        }
    }
    // Safe conversion methods
    private static Long safeLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return Long.parseLong(obj.toString());
    }

    private static Integer safeInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return Integer.parseInt(obj.toString());
    }

    private static Double safeDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.parseDouble(obj.toString());
    }

    private static String safeString(Object obj) {
        if (obj == null) return "";
        return obj.toString();
    }

    private static LocalDate safeLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate();
        if (obj instanceof java.util.Date) return ((java.util.Date) obj).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return LocalDate.parse(obj.toString());
    }
}
