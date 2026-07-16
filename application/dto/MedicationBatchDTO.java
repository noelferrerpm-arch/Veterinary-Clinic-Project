package cat.tecnocampus.application.dto;

import java.time.LocalDate;

public record MedicationBatchDTO (
        Long batchId,
        Long medicationId,
        int lotNumber,
        LocalDate receivedDate,
        LocalDate expiryDate,
        int initialQuantity,
        int currentQuantity,
        double purchagePricePerUnit,
        String storageLocation,
        int reorderThreshold){
}
