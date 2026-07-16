package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;
@Entity
@Table(name="discount")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    @Column(name="code", unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private DiscountType type;

    @Column(name="discount_value") // canviar el nom del camp perque es una paraula reservada de sql
    private double discountValue;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name="max_uses")
    private int maxUses;

    @Column(name="uses_count")
    private int usesCount;

    @ManyToMany(mappedBy = "discounts")
    private Set<Invoice> invoices;

    //Can only be applied to either promotion or loyalty tier
    @ManyToOne
    @JoinColumn(name="promotionId", nullable = false)
    private Promotion promotion;

    @ManyToOne
    private LoyaltyTier loyalty;

    public Discount() {}

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidDataException("Discount code cant be null.");
        }
        this.code = code;
    }

    public DiscountType getType() {
        return this.type;
    }

    public void setType(DiscountType type) {
        this.type = type;
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

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double value) {
        if (value<= 0) {
            throw new InvalidDataException("Discount value must be positive");
        }
        this.discountValue = value;
    }



    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        if (maxUses <= 0) {
            throw new InvalidDataException("Maxim uses must be postivie.");
        }
        this.maxUses = maxUses;
    }

    public int getUsesCount() {
        return usesCount;
    }

    public void setUsesCount(int usesCount) {
        if (usesCount < 0) {
            throw new InvalidDataException("Uses count cant be negative.");
        }
        this.usesCount = usesCount;
    }

    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public LoyaltyTier getLoyalty() {
        return loyalty;
    }

    public void setLoyalty(LoyaltyTier loyalty) {
        this.loyalty = loyalty;
    }

}
