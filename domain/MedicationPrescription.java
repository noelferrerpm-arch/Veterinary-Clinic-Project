package cat.tecnocampus.domain;

import jakarta.persistence.*;

@Entity()
@Table(name="medication_prescription")
public class MedicationPrescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private long prescriptionId;
    @Column(name = "quantity_prescribed")
    private int quantityPrescribed;
    @Column(name = "dosage_instructios")
    private String dosageInstructions;
    @Column(name = "duration")
    private int duration;

    @OneToOne
    @JoinColumn(name="visit_id", referencedColumnName = "visitId")
    private Visit visit;

    @ManyToOne
    @JoinColumn(name="medication_id")
    private Medication medication;

    public MedicationPrescription(int quantityPrescribed, String dosageInstructions, int duration, Visit visit, Medication medication){
        this.quantityPrescribed=quantityPrescribed;
        this.dosageInstructions=dosageInstructions;
        this.duration=duration;
        this.visit=visit;
        this.medication=medication;
    }
    public MedicationPrescription(Long prescriptionId,int quantityPrescribed, String dosageInstructions, int duration, Visit visit, Medication medication){
        this.prescriptionId=prescriptionId;
        this.quantityPrescribed=quantityPrescribed;
        this.dosageInstructions=dosageInstructions;
        this.duration=duration;
        this.visit=visit;
        this.medication=medication;
    }

    public MedicationPrescription() {

    }

    public long getPrescriptionId(){
        return this.prescriptionId;
    }

    public int getQuantityPrescribed(){
        return this.quantityPrescribed;
    }
    public String getDosageInstructions(){
        return this.dosageInstructions;
    }
    public int getDuration(){
        return this.duration;
    }
    public Visit getVisit(){
        return this.visit;
    }
    public Medication getMedication(){
        return this.medication;
    }
    public void setVisit(Visit visit){
        this.visit=visit;
    }
    public void setDosageInstructions(String dosageInstructions){
        this.dosageInstructions=dosageInstructions;
    }
    public void setDuration(int duration){
        this.duration=duration;
    }
    public void setQuantityPrescribed(int quantityPrescribed){
        this.quantityPrescribed=quantityPrescribed;
    }

    public void setMedication(Medication medication){
        this.medication=medication;
    }
}


