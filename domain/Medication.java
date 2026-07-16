package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.IncompatibleMedicationException;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity()
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_id")
    private Long medicationId;
    @Column(name = "name")
    private String name;
    @Column(name = "active_ingredient")
    private String activeIngredient;
    @Column(name = "dosage_unit")
    private int dosageUnit;
    @Column(name = "unit_price")
    private double unitPrice;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicationPrescription> prescription;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Medication> incompatibilities;

    public Medication() {}

    public Long getMedicationID() {
        return medicationId;
    }

    public void setMedicationId(Long medicationId) {
        if (medicationId==null|| medicationId <= 0) {
            throw new InvalidDataException("Medication ID inválido");
        }

        this.medicationId = medicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name==null || name.isEmpty()){
            throw new InvalidDataException("Medication name cannot be null or empty");
        }
        this.name = name;
    }

    public String getActiveIngredient() {
        return activeIngredient;
    }
    public void setActiveIngredient(String activeIngredient) {
        if(activeIngredient==null || activeIngredient.isEmpty()){
            throw new InvalidDataException("Medication description cannot be null or empty");
        }

        this.activeIngredient = activeIngredient;
    }

    public int getDosageUnit() {
        return dosageUnit;
    }
    public void setDosageUnit(int dosageUnit) {
        if(dosageUnit<=0){
            throw new InvalidDataException("Medication dosage must be more than 0");
        }
        this.dosageUnit = dosageUnit;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(double unitPrice) {
        if(unitPrice<=0){
            throw new InvalidDataException("Medication price must be more than 0");
        }
        this.unitPrice = unitPrice;
    }
    public Medication( String name, String activeIngredient, int dosageUnit, double unitPrice) {
        this.name = name;
        this.activeIngredient = activeIngredient;
        this.dosageUnit = dosageUnit;
        this.unitPrice = unitPrice;
    }


    public List<Medication> getIncompatibilities(){
        return incompatibilities;
    }

    public void addIncompatibilities(Medication medication) {
        if(incompatibilities.contains(medication)){
            throw new InvalidDataException("Medication is already incompatible");
        }
        if(medication.equals(this)){
            throw new InvalidDataException("Medication cant be incompatible to itselfs");
        }
        incompatibilities.add(medication);

    }

    public void removeIncompatibilities(Medication medication) {
        if(incompatibilities.contains(medication)){
            incompatibilities.remove(medication);
        }else{
            throw new InvalidDataException("Medication is not incompatible");
        }
    }


    public boolean checkIncompatibilities(Pet pet) {
        for(MedicationPrescription medicationPrescription : pet.getPrescriptions()){
            if(incompatibilities.contains(medicationPrescription.getMedication())){
                return false;
            }
        }
        return true;
    }
}
