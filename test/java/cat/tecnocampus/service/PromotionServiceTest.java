// language: java
package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.DiscountDTO;
import cat.tecnocampus.application.dto.PromotionDTO;
import cat.tecnocampus.application.service.PromotionService;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.Discount;
import cat.tecnocampus.domain.DiscountType;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PromotionServiceTest {

    @Autowired
    private PromotionService promotionService;

    @Test
    public void create_get_update_and_delete_promotion_flow() {
        // create
        PromotionDTO toCreate = new PromotionDTO(
                0L,
                "PromoFullTest",
                "Desc prueba full",
                "CODE_FULL",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                new ArrayList<>()
        );

        PromotionDTO created = promotionService.createPromotion(toCreate);
        assertNotNull(created);
        assertTrue(created.promotionId() > 0);

        // get by id
        PromotionDTO fetched = promotionService.getPromotionById(created.promotionId());
        assertEquals("PromoFullTest", fetched.name());

        // update promotion
        PromotionDTO updateDto = new PromotionDTO(
                created.promotionId(),
                "PromoFullTestUpdated",
                "Desc actualizada",
                "CODE_FULL",
                created.startDate(),
                created.endDate(),
                created.discounts()
        );

        PromotionDTO updated = promotionService.updatePromotion(created.promotionId(), updateDto);
        assertEquals("PromoFullTestUpdated", updated.name());

        // get all contains
        List<PromotionDTO> all = promotionService.getAllPromotions();
        assertTrue(all.stream().anyMatch(p -> p.promotionId() == created.promotionId()));

        // delete
        promotionService.deletePromotion(created.promotionId());
        assertThrows(NotFoundException.class, () -> promotionService.getPromotionById(created.promotionId()));
    }

    @Test
    public void add_update_remove_discount_flow() {
        // prepare promotion
        PromotionDTO promo = promotionService.createPromotion(new PromotionDTO(
                0L,
                "PromoWithDiscounts",
                "Para descuentos",
                "DISC_PROMO",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new ArrayList<>()
        ));
        long promoId = promo.promotionId();

        // build domain Discount -> map to DTO to avoid depending on DTO constructor order
        Discount d = new Discount();
        d.setCode("D1");
        d.setStartDate(LocalDate.now());
        d.setEndDate(LocalDate.now().plusDays(2));
        d.setDiscountValue(0.1);
        d.setMaxUses(10);
        d.setType(DiscountType.PERCENTAGE);
        d.setUsesCount(0);

        DiscountDTO createdDiscount = promotionService.addDiscountToPromotion(promoId, MapperHelper.mapDiscountDTO(d));
        assertNotNull(createdDiscount);
        assertEquals("D1", createdDiscount.code());

        // get discounts by promotion
        List<DiscountDTO> discounts = promotionService.getDiscountsByPromotionId(promoId);
        assertTrue(discounts.stream().anyMatch(dd -> Objects.equals(dd.code(), createdDiscount.code())));

        // update discount
        DiscountDTO updatedInput = new DiscountDTO(
                "D1-UPDATED",
                DiscountType.PERCENTAGE,
                0.2,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                20,
                0
        );
        DiscountDTO updated = promotionService.updateDiscountInPromotion(promoId, 2, updatedInput);
        assertEquals("D1-UPDATED", updated.code());
        assertEquals(0.2, updated.discountValue());

        // remove discount
        promotionService.removeDiscountFromPromotion(promoId, 2);
        List<DiscountDTO> afterRemoval = promotionService.getDiscountsByPromotionId(promoId);
        assertTrue(afterRemoval.stream().noneMatch(dd -> Objects.equals(dd.code(), "D1-UPDATED")));

        // cleanup
        promotionService.deletePromotion(promoId);
    }

    @Test
    public void add_duplicate_discount_code_throws_InvalidDataException() {
        PromotionDTO promo = promotionService.createPromotion(new PromotionDTO(
                0L,
                "PromoDupCode",
                "Dup code",
                "DUP_PROMO",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new ArrayList<>()
        ));
        long promoId = promo.promotionId();

        Discount d1 = new Discount();
        d1.setCode("SAME");
        d1.setStartDate(LocalDate.now());
        d1.setEndDate(LocalDate.now().plusDays(1));
        d1.setDiscountValue(0.1);
        d1.setMaxUses(5);
        d1.setType(DiscountType.PERCENTAGE);
        d1.setUsesCount(0);

        DiscountDTO dto1 = MapperHelper.mapDiscountDTO(d1);
        promotionService.addDiscountToPromotion(promoId, dto1);

        // second with same code must throw InvalidDataException
        Discount d2 = new Discount();
        d2.setCode("SAME");
        d2.setStartDate(LocalDate.now());
        d2.setEndDate(LocalDate.now().plusDays(2));
        d2.setDiscountValue(0.15);
        d2.setMaxUses(5);
        d2.setType(DiscountType.PERCENTAGE);
        d2.setUsesCount(0);

        DiscountDTO dto2 = MapperHelper.mapDiscountDTO(d2);
        assertThrows(InvalidDataException.class, () -> promotionService.addDiscountToPromotion(promoId, dto2));

        promotionService.deletePromotion(promoId);
    }

    @Test
    public void analytics_and_non_existing_behaviour() {
        // analytics should not throw
        var analytics = promotionService.getAnalytics();
        assertNotNull(analytics);

        // delete non existing promotion should throw NotFoundException
        assertThrows(NotFoundException.class, () -> promotionService.deletePromotion(999999L));
    }
}
