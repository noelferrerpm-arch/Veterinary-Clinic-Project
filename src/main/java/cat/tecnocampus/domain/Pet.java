package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petId;

    private String name;
    private LocalDate dateOfBirth;
    private String gender;
    private String breed;
    private String color;
    private String weight;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_owner_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pet_owner"))
    private PetOwner petOwner;

    private String microchipId;

    @ManyToOne(fetch = FetchType.LAZY)
    private PetType petType;


    @OneToMany(mappedBy = "pet",
          cascade = CascadeType.ALL,
           orphanRemoval = true)
   private List<Visit> visits = new ArrayList<>();

    @ManyToMany
    private List<MedicationPrescription> prescriptions = new ArrayList<>();

    public Pet() {}

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        if(breed == null || breed.isBlank()) {
            throw new InvalidDataException("breed must not be blank");
        }
        this.breed = breed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public PetOwner getPetOwner() {
        return petOwner;
    }

    public void setPetOwner(PetOwner petOwner) {
        this.petOwner = petOwner;
    }

    public String getMicrochipId() {
        return microchipId;
    }

    public void setMicrochipId(String microchipId) {
        this.microchipId = microchipId;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    public List<MedicationPrescription> getPrescriptions() {
        return prescriptions;
    }

    public void addPrescription(MedicationPrescription prescription) {
        if(prescription != null) {
            if(!prescriptions.contains(prescription)) {
                prescriptions.add(prescription);
            }else{
                throw new InvalidDataException("prescription already exists");
            }
        }else{
            throw new InvalidDataException("prescription is null");
        }
    }

    public void removePrescription(MedicationPrescription prescription) {
        if(prescription != null) {
            if(prescriptions.contains(prescription)) {
                prescriptions.remove(prescription);
            }else{
                throw new InvalidDataException("prescription didnt exists");
            }
        }else{
            throw new InvalidDataException("prescription is null");
        }
    }
}
