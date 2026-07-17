package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity(name="Promotion")
@Table(name="promotion")
public class Promotion {
    @Id
    @GeneratedValue
    private Long promotionId;

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="discountCode")
    private String discountCode;

    @Column(name="startDate")
    private LocalDate startDate;

    @Column(name="endDate")
    private LocalDate endDate;
    //One promotion can have several discounts
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Discount> discounts;

    public Promotion(String name, String description, String discountCode, LocalDate startDate, LocalDate endDate, List<Discount> discounts) {
        this.name = name;
        this.description = description;
        this.discountCode = discountCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discounts = discounts;
    }

    public Promotion() {

    }

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        if(name.isEmpty()||name.isBlank()) {
            throw new InvalidDataException("Ther must be a description");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description.isEmpty()||description.isBlank()) {
            throw new InvalidDataException("Ther must be a description");
        }
        this.description = description;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        if(discountCode.isEmpty()||discountCode.isBlank()){
            throw new InvalidDataException("There must be a discount code");
        }
        this.discountCode = discountCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        if(startDate==null||(endDate!=null)&&startDate.isAfter(endDate)) {
            throw new InvalidDataException("Start date cannot be after end date or null");
        }
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        if(endDate==null||(startDate!=null)&&startDate.isAfter(endDate)) {
            throw new InvalidDataException("End date cannot be before start date or null");
        }

        this.endDate = endDate;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<Discount> discounts) {
        this.discounts = discounts;
    }
}
