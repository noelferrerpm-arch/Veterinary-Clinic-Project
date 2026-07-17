package cat.tecnocampus.application.mapper;

import cat.tecnocampus.application.dto.*;
import cat.tecnocampus.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MapperHelper {
    public static AvailabilityDTO mapAvailabilityDTO(Availability a) {
        return new AvailabilityDTO(a.getAvailabilityId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.getPeriodStart(), a.getPeriodEnd(),
                Optional.ofNullable(a.getAvailabilityExceptions()).map(ae -> ae.stream().map(MapperHelper::mapAvailabilityExceptionDTO).toList()).orElse(List.of()));
    }

    public static AvailabilityExceptionDTO mapAvailabilityExceptionDTO(AvailabilityException a) {
        return new AvailabilityExceptionDTO(a.getAvailabilityExceptionId(), a.getReason(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime(), a.getPeriodStart(), a.getPeriodEnd());
    }


    public static VeterinarianDTO mapVeterinarianDTO(Veterinarian v) {
        return new VeterinarianDTO(v.getPersonId(), v.getFirstName(), v.getLastName(), v.getPhoneNumber(), v.getEmail(), v.getAddress(),
                v.getLicenseNumber(), v.getYearsOfExperience(), v.getAvailability().stream().map(MapperHelper::mapAvailabilityDTO).toList());
    }

    public static MedicationBatchDTO mapMedicationBatchDTO(MedicationBatch m) {
        return new MedicationBatchDTO(m.getBatchId(),m.getMedicationId(), m.getLotNumber(), m.getReceivedDate(), m.getExpiryDate(), m.getInitialQuantity(), m.getCurrentQuantity(), m.getPurchagePricePerUnit(), m.getStorageLocation(), m.getReorderThreshold());
    }


    public static DiscountDTO mapDiscountDTO(Discount d) {
        return new DiscountDTO(d.getCode(), d.getType(), d.getDiscountValue(), d.getStartDate(), d.getEndDate(), d.getMaxUses(), d.getUsesCount());
    }

    public static VisitDTO mapVisitDTO(Visit visit) {
        return new VisitDTO(visit.getVisitId(), visit.getStartDate(),visit.getDuration(), visit.getReason(), visit.getPrice(), visit.getDiagnosis(), visit.getNotes(), visit.getStatus(),
                visit.getPet().getPetId(),
                visit.getOwner().getPersonId(),
                visit.getVeterinarian().getPersonId(),
                Optional.ofNullable(visit.getTreatment())
                        .map(Treatment::getTreatmentId)  // devuelve solo el ID
                        .orElse(null), Optional.ofNullable(visit.getInvoice()).map(Invoice::getInvoiceId).orElse(null));
    }

    public static PetDTO mapPetDTO(Pet pet) {
        return new PetDTO(pet.getPetId(), pet.getPetOwner().getPersonId(), Optional.ofNullable(pet.getPetType()).map(MapperHelper::mapPetTypeDTO).orElse(null), pet.getName(), pet.getMicrochipId(), pet.getDateOfBirth(), pet.getGender(), pet.getBreed(), pet.getColor(), pet.getWeight());
    }

    public static PetOwnerDTO mapPetOwnerDTO(PetOwner pO) {
        return new PetOwnerDTO(pO.getPersonId(), pO.getFirstName(), pO.getLastName(), pO.getPhoneNumber(), pO.getEmail(), pO.getAddress(), pO.getLoyaltyPoints(), pO.getLoyaltyTier().getId(),
                Optional.ofNullable(pO.getPets()).map(pet -> pet.stream().map(MapperHelper::mapPetDTO).toList()).orElse(null));
    }

    public static PetTypeDTO mapPetTypeDTO(PetType pT) {
        return new PetTypeDTO(pT.getTypeId(), pT.getName(), pT.getDescription());
    }

    public static MedicationPrescriptionDTO mapMedicationPrescriptionDTO(MedicationPrescription mp){
        return new MedicationPrescriptionDTO(mp.getPrescriptionId(),mp.getQuantityPrescribed(),mp.getDosageInstructions(),mp.getDuration(),mp.getVisit
                ().getVisitId(),mp.getMedication().getMedicationID());
    }


    public static LoyaltyTierDTO mapLoyaltyTierDTO(LoyaltyTier e) {
        return new LoyaltyTierDTO(
                e.getId(),
                e.getTierName(),
                e.getRequiredPoints(),
                e.getDiscountPercentage(),
                e.getBenefitsDescription()
        );
    }

    public static MedicationDTO mapMedicationDTO(Medication m){
        return new MedicationDTO(m.getMedicationID(),m.getName(),m.getActiveIngredient(),m.getDosageUnit(),m.getUnitPrice(),m.getIncompatibilities());
    }

    public static AgendaDTO mapAgendaDTO(Agenda a) {
        return new AgendaDTO(a.getAgendaId(), Optional.ofNullable(a.getVeterinarian()).map(MapperHelper::mapVeterinarianDTO).orElse(null),
                a.getAgendaYear(),
                Optional.ofNullable(a.getScheduledVisits()).map(v -> v.stream().map(MapperHelper::mapVisitDTO).toList()).orElse(List.of()));
    }

    public static InvoiceDTO mapInvoiceDTO(Invoice invoice) {
        return new InvoiceDTO(
                invoice.getInvoiceId(),
                invoice.getInvoiceDate(),
                invoice.getTotalAmount(),
                invoice.getDiscountAmount(),
                invoice.getFinalAmount(),
                invoice.getStatus(),
                Optional.ofNullable(invoice.getPetOwner())
                        .map(PetOwner::getPersonId)  // devuelve solo el ID
                        .orElse(null),
                Optional.ofNullable(invoice.getItems())
                        .map(list -> list.stream()
                                .map(MapperHelper::mapInvoiceItemDTO)
                                .toList())
                        .orElse(List.of()),
                Optional.ofNullable(invoice.getPayments())
                        .map(list -> list.stream()
                                .map(MapperHelper::mapPaymentDTO)
                                .toList())
                        .orElse(List.of()),
                Optional.ofNullable(invoice.getDiscounts())
                        .map(set -> set.stream()
                                .map(Discount::getDiscountId) // solo IDs
                                .collect(java.util.stream.Collectors.toSet()))
                        .orElse(Set.of())
        );
    }


    public static InvoiceItemDTO mapInvoiceItemDTO(InvoiceItem item) {
        return new InvoiceItemDTO(
                item.getItemId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getItemTotal()
        );
    }

    public static PaymentDTO mapPaymentDTO(Payment p) {
        return new PaymentDTO(
                p.getPaymentId(),
                p.getPaymentDate(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getTransactionRef()
        );
    }

    public static TreatmentPrescriptionDTO mapTreatmentPrescriptionDTO(Treatment e) {
        if (e == null) return null;
        return new TreatmentPrescriptionDTO(
                e.getTreatmentId(),
                e.getName(),
                e.getDescription(),
                e.getCost()
        );
    }


}
