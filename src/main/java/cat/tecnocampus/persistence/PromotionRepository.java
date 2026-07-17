package cat.tecnocampus.persistence;
import cat.tecnocampus.application.outputDTO.DiscountAnalyticsDTO;
import cat.tecnocampus.domain.Discount;
import cat.tecnocampus.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Query(value = """
        SELECT 
            d.discount_id,
            d.code,
            d.type,
            d.discount_value,
            d.start_date,
            d.end_date,
            d.max_uses,
            d.uses_count,
            COUNT(DISTINCT di.invoice_id),
            COUNT(DISTINCT i.pet_owner_id),
            COALESCE(SUM(i.discount_amount), 0),
            COALESCE(SUM(i.final_amount), 0),
            COALESCE(SUM(i.total_amount), 0),
            CASE 
                WHEN COALESCE(SUM(i.total_amount), 0) > 0 THEN 
                    (COALESCE(SUM(i.discount_amount), 0) / COALESCE(SUM(i.total_amount), 0)) * 100 
                ELSE 0 
            END,
            CASE 
                WHEN d.max_uses > 0 THEN 
                    (d.uses_count * 100.0 / d.max_uses) 
                ELSE 100 
            END,
            CASE 
                WHEN COUNT(DISTINCT di.invoice_id) > 0 THEN 
                    COALESCE(SUM(i.discount_amount), 0) / COUNT(DISTINCT di.invoice_id) 
                ELSE 0 
            END,
            MIN(i.invoice_date),
            MAX(i.invoice_date)
        FROM discount d
        LEFT JOIN discount_invoice di ON d.discount_id = di.discount_id
        LEFT JOIN invoice i ON di.invoice_id = i.invoice_id AND (i.status != 'CANCELLED' OR i.status IS NULL)
        GROUP BY 
            d.discount_id, 
            d.code, 
            d.type, 
            d.discount_value, 
            d.start_date, 
            d.end_date, 
            d.max_uses, 
            d.uses_count
        ORDER BY COUNT(DISTINCT di.invoice_id) DESC, COALESCE(SUM(i.discount_amount), 0) DESC
        """, nativeQuery = true)
    List<Object[]> getAnalyticsRaw();
}
