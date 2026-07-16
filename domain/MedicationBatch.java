package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;

import java.time.LocalDate;
@Entity
@Table(name="medication_batch")
public class MedicationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="batch_id")
    private Long batchId;
    @Column(name = "medication_Id")
    private Long medicationId;
    @Column(name = "lotNumber")
    private int lotNumber;
    @Column(name = "received_date")
    private LocalDate receivedDate;
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Column(name = "initial_quantity")
    private int initialQuantity;
    @Column(name = "current_quantity")
    private int currentQuantity;
    @Column(name = "purchage_Price_Per_Unit")
    private double purchagePricePerUnit;
    @Column(name = "storage_Location")
    private String storageLocation;
    @Column(name = "reorder_threshold")
    private int reorderThreshold;
    @OneToOne
    private Invoice invoice;






    public Long getMedicationId(){
        return medicationId;
    }
    public Long getBatchId() {return batchId;}
    public int getLotNumber() {return lotNumber;}
    public LocalDate getReceivedDate() {return receivedDate;}
    public LocalDate getExpiryDate() {return expiryDate;}
    public int getInitialQuantity() {return initialQuantity;}
    public int getCurrentQuantity() {return currentQuantity;}
    public double getPurchagePricePerUnit() {return purchagePricePerUnit;}
    public String getStorageLocation() {return storageLocation;}
    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setMedicationId(Long medicationId) {
        if(medicationId == null || medicationId <= 0){
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.medicationId = medicationId;
    }
    public void setBatchId(Long batchId) {
        if(batchId == null || this.batchId == null){
            throw new InvalidDataException("Id cant be null or less than 0");

        }
        this.batchId = batchId;
    }
    public void setLotNumber(int lotNumber) {
        if(lotNumber <= 0){
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.lotNumber = lotNumber;
    }
    public void setReceivedDate(LocalDate receivedDate) {
        if(receivedDate == null ||(expiryDate!=null && expiryDate.isBefore(receivedDate))){
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.receivedDate = receivedDate;
    }
    public void setExpiryDate(LocalDate expiryDate) {
        if(expiryDate == null || expiryDate.isBefore(LocalDate.now()) ||(receivedDate!=null && expiryDate.isBefore(receivedDate))){
            throw new InvalidDataException("Id cant be null or less than 0");

        }
        this.expiryDate = expiryDate;
    }
    public void setInitialQuantity(int initialQuantity) {
        if(initialQuantity <= 0) {
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.initialQuantity=initialQuantity;
    }
    public void setCurrentQuantity(int currentQuantity) {
        if(currentQuantity <= 0){
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.currentQuantity=currentQuantity;
    }
    public void setPurchagePricePerUnit(double purchagePricePerUnit) {
        if(purchagePricePerUnit <= 0){
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.purchagePricePerUnit = purchagePricePerUnit;

    }
    public void setStorageLocation(String storageLocation) {
        if(storageLocation == null || storageLocation.isEmpty()){
            throw new InvalidDataException("Storage location cant be null or empty");
        }
        this.storageLocation = storageLocation;
    }
    public void setReorderThreshold(int reorderThreshold) {
        if(reorderThreshold <= 0){
            throw new InvalidDataException("Id cant be null or less than 0");
        }
        this.reorderThreshold = reorderThreshold;
    }




    public MedicationBatch(Long medication_id, int lotNumber, LocalDate receivedDate, LocalDate expiryDate, int initialQuantity, double purchagePricePerUnit, String storageLocation, int reorderThreshold) {
        this.medicationId = medication_id;
        this.lotNumber = lotNumber;
        this.receivedDate = receivedDate;
        this.expiryDate = expiryDate;
        this.initialQuantity = initialQuantity;
        this.currentQuantity = initialQuantity;
        this.purchagePricePerUnit = purchagePricePerUnit;
        this.storageLocation = storageLocation;
        this.reorderThreshold = reorderThreshold;

    }


    public MedicationBatch betterOption(MedicationBatch toGive, int quantity){
        if(toGive==null){
            return this;
        }
        if(this.getExpiryDate().isBefore(toGive.getExpiryDate())){
            if(this.getCurrentQuantity()>quantity){
                return this;
            }
        }
        return toGive;
    }


    public MedicationBatch() {

    }







}
