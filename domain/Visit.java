package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="visit")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long visitId;

    private LocalDateTime startDate;
    private int duration;
    private String reason;
    private double price;

    @Column(name = "diagnosis")
    private String diagnosis;
    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    private Status_VPR status = Status_VPR.Scheduled;
    //A pet can have multiple visits
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
    //A pet owner can have multiple visits
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private PetOwner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id")
    private Veterinarian veterinarian;

    @ManyToOne
    @JoinColumn(name = "agendaId")
    private Agenda agenda;

    @ManyToOne
    @JoinColumn(name="treatmentId")
    private Treatment treatment;

    @OneToMany
    private List<MedicationPrescription> prescriptions;
    @OneToOne
    private Invoice invoice;

    public Agenda getAgenda() {
        return agenda;
    }

    public Status_VPR getStatus() {
        return status;
    }

    public void setStatus(Status_VPR status) {
        this.status = status;
    }

    public void setAgenda(Agenda agenda) {
        this.agenda = agenda;
    }

    public Visit(){}

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        if(diagnosis==null|| diagnosis.isEmpty()){
            throw new InvalidDataException("Diagnosis can't be empty");
        }
        this.diagnosis = diagnosis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if(notes==null|| notes.isEmpty()){
            throw new InvalidDataException("Note can't be empty");
        }
        this.notes = notes;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public PetOwner getOwner() {
        return owner;
    }

    public void setOwner(PetOwner owner) {
        this.owner = owner;
    }

    public Veterinarian getVeterinarian() {
        return veterinarian;
    }

    public void setVeterinarian(Veterinarian veterinarian) {
        this.veterinarian = veterinarian;
    }

    public Treatment getTreatment(){
        return treatment;
    }

    public void setTreatment(Treatment treatment){
        this.treatment=treatment;
    }

    public List<MedicationPrescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<MedicationPrescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public void initiateVisit(){
        if(status.equals(Status_VPR.Scheduled)) {
            status=Status_VPR.In_Progress;

        } else {
            throw new InvalidDataException("Visit must be scheduled to be initiated");
        }
    }
    public void completeVisit(){
        if(status.equals(Status_VPR.In_Progress)) {
            status=Status_VPR.Completed;

        } else {
            throw new InvalidDataException("Visit must be in progress to be completed");
        }
    }


}
