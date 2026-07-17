package cat.tecnocampus.domain;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity(name = "LoyaltyTier")
@Table(
        name = "loyalty_tier",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_loyalty_tier_name", columnNames = "tier_name")
        },
        indexes = {
                @Index(name = "idx_loyalty_required_points", columnList = "required_points")
        }
)
public class LoyaltyTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name="tier_name", nullable=false, length=30)
    private String tierName;

    @PositiveOrZero
    @Column(nullable=false)
    private int requiredPoints;

    private double discountPercentage;

    @Column(name = "benefits_description")
    private String benefitsDescription;


    @OneToMany(mappedBy = "loyaltyTier", fetch = FetchType.LAZY)
    private List<PetOwner> petOwners;

    public LoyaltyTier() { }

    public LoyaltyTier(String tierName, int requiredPoints, double discountPercentage, String benefitsDescription) {
        this.tierName = tierName;
        this.requiredPoints = requiredPoints;
        this.discountPercentage = discountPercentage;
        this.benefitsDescription = benefitsDescription;
    }

    public void setId(Long id){ this.id = id; }
    public Long getId() { return id; }
    public String getTierName() { return tierName; }
    public void setTierName(String tierName) {
        if(tierName.isBlank()||tierName.isEmpty()){
            throw new InvalidDataException("Loyalty tier must have a name");
        }
        this.tierName = safeTrim(tierName);
    }
    public int getRequiredPoints() { return requiredPoints; }
    public void setRequiredPoints(int requiredPoints) {
        if(requiredPoints<=0){
            throw new InvalidDataException("Required points must be positive");
        }
        this.requiredPoints = requiredPoints; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) {
        if(discountPercentage<=0||discountPercentage>=100){
            throw new InvalidDataException("Discount percentatge must be between 0 and 100");
        }

        this.discountPercentage = discountPercentage; }
    public String getBenefitsDescription() { return benefitsDescription; }
    public void setBenefitsDescription(String benefitsDescription) {
        if(benefitsDescription.isBlank()||benefitsDescription.isEmpty()){
            throw new InvalidDataException("benefitsDescription cant be empty");
        }
        this.benefitsDescription = benefitsDescription; }
    public List<PetOwner> getPetOwners() { return petOwners; }
    public void setPetOwners(List<PetOwner> petOwners) {
        this.petOwners = petOwners;
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
