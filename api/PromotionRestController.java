package cat.tecnocampus.api;

import cat.tecnocampus.application.dto.DiscountDTO;
import cat.tecnocampus.application.dto.PromotionDTO;
import cat.tecnocampus.application.outputDTO.DiscountAnalyticsDTO;
import cat.tecnocampus.domain.Discount;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cat.tecnocampus.application.service.PromotionService;

import java.util.List;

@RestController
@RequestMapping("/api/promotion")
public class PromotionRestController {
    private final PromotionService promotionService;

    public PromotionRestController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionDTO createPromotion(@RequestBody PromotionDTO promotionDTO) {
        return this.promotionService.createPromotion(promotionDTO);
    }

    @PostMapping("/{promotionId}/discount/")
    @ResponseStatus(HttpStatus.CREATED)
    public DiscountDTO addDiscountToPromotion(@PathVariable long promotionId, @RequestBody DiscountDTO discount){
        return this.promotionService.addDiscountToPromotion(promotionId,discount);
    }

    @DeleteMapping("/{promotionId}/discount/{discountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDiscountFromPromotion(@PathVariable long promotionId, @PathVariable long discountId){
        this.promotionService.removeDiscountFromPromotion(promotionId,discountId);
    }

    @PutMapping("/{promotionId}/discount/{discountId}")
    public DiscountDTO updateDiscountInPromotion(@PathVariable long promotionId, @PathVariable long discountId, @RequestBody DiscountDTO discountDTO){
        return this.promotionService.updateDiscountInPromotion(promotionId,discountId,discountDTO);
    }

    @GetMapping
    public List<PromotionDTO> getAllPromotions(){
        return  this.promotionService.getAllPromotions();
    }

    @GetMapping("/{promotionId}")
    public PromotionDTO getPromotionById(@PathVariable long promotionId){
        return this.promotionService.getPromotionById(promotionId);
    }

    @GetMapping("/{promotionId}/discounts")
    public List<DiscountDTO> getDiscountsByPromotionId(@PathVariable long promotionId) {
        return this.promotionService.getDiscountsByPromotionId(promotionId);
    }

    @PutMapping("/{promotionId}")
    public PromotionDTO updatePromotion(@PathVariable long promotionId, @RequestBody PromotionDTO promotionDTO){
        return this.promotionService.updatePromotion(promotionId,promotionDTO);
    }

    @DeleteMapping("/{promotionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable long promotionId){
        this.promotionService.deletePromotion(promotionId);
    }

    @GetMapping("/analytics")
    public List<DiscountAnalyticsDTO> getAnalytics(){
        return this.promotionService.getAnalytics();
    }


}
