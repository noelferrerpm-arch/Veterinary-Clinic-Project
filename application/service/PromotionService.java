package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.DiscountDTO;
import cat.tecnocampus.application.dto.PromotionDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.application.mapper.PromotionDiscountMapper;
import cat.tecnocampus.application.outputDTO.DiscountAnalyticsDTO;
import cat.tecnocampus.domain.Discount;
import cat.tecnocampus.domain.Promotion;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;

import cat.tecnocampus.persistence.PromotionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;

    }

    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        Promotion p = new Promotion();
        p.setName(promotionDTO.name());
        p.setDescription(promotionDTO.description());
        p.setDiscountCode(promotionDTO.discountCode());
        p.setStartDate(promotionDTO.startDate());
        p.setEndDate(promotionDTO.endDate());
        p.setDiscounts(new ArrayList<>());
        promotionRepository.save(p);
        return PromotionDiscountMapper.mapToDTO(p);
    }

    public void removeDiscountFromPromotion(long promotionId, long discountId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));
        boolean removed = promotion.getDiscounts()
                .removeIf(discount -> discount.getDiscountId() == discountId);

        if (!removed) {
            throw new InvalidParameterException("The discount with ID: " + discountId +
                    " is not associated with the promotion with ID: " + promotionId);
        }
    }

    public List<PromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(PromotionDiscountMapper::mapToDTO)
                .toList();
    }

    public PromotionDTO getPromotionById(long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));
        return PromotionDiscountMapper.mapToDTO(promotion);
    }

    public List<DiscountDTO> getDiscountsByPromotionId(long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));
        return promotion.getDiscounts().stream()
                .map(MapperHelper::mapDiscountDTO)
                .toList();
    }

    public DiscountDTO updateDiscountInPromotion(long promotionId, long discountId, DiscountDTO discountDTO) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));

        Discount discount = promotion.getDiscounts().stream()
                .filter(d -> d.getDiscountId() == discountId)
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException(
                        "The discount with ID: " + discountId + " is not associated with the promotion with ID: " + promotionId));


        discount.setCode(discountDTO.code());
        discount.setStartDate(discountDTO.startDate());
        discount.setEndDate(discountDTO.endDate());
        discount.setDiscountValue(discountDTO.discountValue());
        discount.setMaxUses(discountDTO.maxUses());
        discount.setType(discountDTO.type());
        discount.setUsesCount(discountDTO.usesCount());

        return MapperHelper.mapDiscountDTO(discount);
    }

    public PromotionDTO updatePromotion(long promotionId, PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));

        promotion.setName(promotionDTO.name());
        promotion.setDescription(promotionDTO.description());
        promotion.setDiscountCode(promotionDTO.discountCode());
        promotion.setStartDate(promotionDTO.startDate());
        promotion.setEndDate(promotionDTO.endDate());


        return PromotionDiscountMapper.mapToDTO(promotion);
    }

    public void deletePromotion(long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));
        promotionRepository.delete(promotion);
    }


    public DiscountDTO addDiscountToPromotion(long promotionId, DiscountDTO discount) {
        Promotion promotion = promotionRepository.findById(promotionId)
                 .orElseThrow(() -> new NotFoundException("There is no Promotion with that ID: " + promotionId));
        boolean codeExists = promotion.getDiscounts().stream()
                .anyMatch(d -> d.getCode().equalsIgnoreCase(discount.code()));
        if (codeExists) {
            throw new InvalidDataException("There is already a discount with the code '" + discount.code() + "' in this promotion.");
        }

        Discount newDiscount = new Discount();
        newDiscount.setCode(discount.code());
        newDiscount.setStartDate(discount.startDate());
        newDiscount.setEndDate(discount.endDate());
        newDiscount.setDiscountValue(discount.discountValue());
        newDiscount.setMaxUses(discount.maxUses());
        newDiscount.setType(discount.type());
        newDiscount.setUsesCount(discount.usesCount());
        newDiscount.setPromotion(promotion);
        promotion.getDiscounts().add(newDiscount);
        promotionRepository.save(promotion);
        return MapperHelper.mapDiscountDTO(newDiscount);
    }

    public List<DiscountAnalyticsDTO> getAnalytics() {
        List<Object[]> rawdata= promotionRepository.getAnalyticsRaw();
        return PromotionDiscountMapper.mapDataToDTO(rawdata);
    }
}
