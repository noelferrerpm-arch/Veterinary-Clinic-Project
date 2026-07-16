package cat.tecnocampus.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pet_owner")
public class PetOwner extends Person {

    @Column(name = "loyalty_points", nullable = false)
    private int loyaltyPoints = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loyalty_tier_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_owner_loyalty_tier"))
    private LoyaltyTier loyaltyTier;

    @OneToMany(mappedBy = "petOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    public PetOwner() {}

    // getters/setters (usa getId() de Person como PK)
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public LoyaltyTier getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(LoyaltyTier loyaltyTier) { this.loyaltyTier = loyaltyTier; }

    public List<Pet> getPets() { return pets; }
    public void setPets(List<Pet> pets) { this.pets = pets; }

    public void addPet(Pet p){ if(p!=null){ pets.add(p); p.setPetOwner(this);} }
    public void removePet(Pet p){ if(p!=null){ pets.remove(p); p.setPetOwner(null);} }
}
