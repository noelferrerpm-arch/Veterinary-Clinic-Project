package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.MedicationSaleRequest;
import cat.tecnocampus.persistence.VisitRepository;
import cat.tecnocampus.persistence.MedicationRepository;
import cat.tecnocampus.application.dto.InvoiceDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.*;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final PetOwnerRepository petOwnerRepo;
    private final PaymentRepository paymentRepo;
    private final LoyaltyTierRepository loyaltyTierRepo;
    private final VisitRepository visitRepo;
    private final MedicationRepository medicationRepo;


    public InvoiceService(InvoiceRepository invoiceRepo, PetOwnerRepository petOwnerRepo,
                          PaymentRepository paymentRepo, LoyaltyTierRepository loyaltyTierRepo, VisitRepository visitRepo, MedicationRepository medicationRepo) {
        this.invoiceRepo = invoiceRepo;
        this.petOwnerRepo = petOwnerRepo;
        this.paymentRepo = paymentRepo;
        this.loyaltyTierRepo =loyaltyTierRepo;
        this.visitRepo = visitRepo;
        this.medicationRepo = medicationRepo;
    }

    public InvoiceDTO createInvoice(InvoiceDTO invoiceDTO) throws InvalidDataException {

        PetOwner owner = invoiceDTO.petOwnerId() != null
                ? petOwnerRepo.findById(invoiceDTO.petOwnerId())
                .orElseThrow(() -> new NotFoundException("PetOwner not found: " + invoiceDTO.petOwnerId()))
                : null;

        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setTotalAmount(invoiceDTO.totalAmount());
        invoice.setPetOwner(owner);
        invoice.setStatus(invoiceDTO.status());

        // descuento loyalty tier
        applyLoyaltyDiscount(invoice, owner);

        // creo items
        List<InvoiceItem> items = invoiceDTO.items().stream().map(dto -> {
            InvoiceItem item = new InvoiceItem();
            item.setDescription(dto.description());
            item.setQuantity(dto.quantity());
            item.setUnitPrice(dto.unitPrice());
            item.setItemTotal(dto.itemTotal());
            item.setInvoice(invoice);
            return item;
        }).toList();

        // creo payments
        List<Payment> payments = invoiceDTO.payments().stream().map(dto -> {
            Payment p = new Payment();
            p.setPaymentDate(dto.paymentDate());
            p.setAmount(dto.amount());
            p.setPaymentMethod(dto.paymentMethod());
            p.setTransactionRef(dto.transactionRef());
            p.setInvoice(invoice);
            return p;
        }).toList();

        invoice.setItems(items);
        invoice.setPayments(payments);

        Invoice saved = invoiceRepo.save(invoice);
        return MapperHelper.mapInvoiceDTO(saved);
    }

    private void applyLoyaltyDiscount(Invoice invoice, PetOwner owner) {
        if (owner == null || owner.getLoyaltyTier() == null) {
            invoice.setDiscountAmount(0.0);
            invoice.setFinalAmount(invoice.getTotalAmount());
            return;
        }

        double percentage = owner.getLoyaltyTier().getDiscountPercentage();
        double discount = (invoice.getTotalAmount() * percentage) / 100.0;

        invoice.setDiscountAmount(discount);
        invoice.setFinalAmount(invoice.getTotalAmount() - discount);
    }

    public InvoiceDTO updateInvoice(Long id, InvoiceDTO invoiceDTO) {

        Invoice existing = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice not found: " + id));

        existing.setTotalAmount(invoiceDTO.totalAmount());
        applyLoyaltyDiscount(existing, existing.getPetOwner());
        existing.setStatus(invoiceDTO.status());

        Invoice saved = invoiceRepo.save(existing);
        return MapperHelper.mapInvoiceDTO(saved);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public InvoiceDTO getInvoice(Long id) {
        Invoice invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice not found: " + id));
        return MapperHelper.mapInvoiceDTO(invoice);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<InvoiceDTO> listAll() {
        return ((List<Invoice>) invoiceRepo.findAll())
                .stream()
                .map(MapperHelper::mapInvoiceDTO)
                .toList();
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice not found: " + id));

        if (invoice.getPayments() != null && !invoice.getPayments().isEmpty()) {
            throw new InvalidDataException("Cannot delete an invoice that has registered payments");
        }

        invoiceRepo.delete(invoice);
    }

    public InvoiceDTO markInvoiceAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found: " + invoiceId));

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new InvalidDataException("Invoice is already marked as paid");
        }

        double totalPaid = invoice.getPayments() != null
                ? invoice.getPayments().stream().mapToDouble(Payment::getAmount).sum()
                : 0.0;

        if (totalPaid < invoice.getFinalAmount()) {
            throw new InvalidDataException("Total payments (" + totalPaid + ") do not cover final amount (" + invoice.getFinalAmount() + ")");
        }

        invoice.setStatus("PAID");

        if (invoice.getPetOwner() != null) {
            earnLoyaltyPoints(invoice.getPetOwner(), invoice.getFinalAmount());
        }

        Invoice saved = invoiceRepo.save(invoice);
        return MapperHelper.mapInvoiceDTO(saved);
    }

    private void earnLoyaltyPoints(PetOwner owner, double finalAmount) {

        int pointsEarned = (int) (finalAmount / 10.0);

        if (pointsEarned > 0) {
            owner.setLoyaltyPoints(owner.getLoyaltyPoints() + pointsEarned);

            reevaluateLoyaltyTier(owner);

            petOwnerRepo.save(owner);
        }
    }

    private void reevaluateLoyaltyTier(PetOwner owner) {
        int currentPoints = owner.getLoyaltyPoints();

        List<LoyaltyTier> allTiers = ((List<LoyaltyTier>) loyaltyTierRepo.findAll())
                .stream()
                .sorted((t1, t2) -> Integer.compare(t2.getRequiredPoints(), t1.getRequiredPoints()))
                .toList();

        for (LoyaltyTier tier : allTiers) {
            if (currentPoints >= tier.getRequiredPoints()) {
                owner.setLoyaltyTier(tier);
                return;
            }
        }

    }

    @Transactional
    public InvoiceDTO generateInvoiceFromVisit(Long visitId) {

        Visit visit = visitRepo.findById(visitId)
                .orElseThrow(() -> new NotFoundException("Visit not found: " + visitId));

        if (visit.getStatus() != Status_VPR.Completed) {
            throw new InvalidDataException("Only completed visits can generate invoices");
        }

        PetOwner owner = visit.getOwner();
        if (owner == null) {
            throw new InvalidDataException("Visit does not have an associated PetOwner");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus("Unpaid");
        invoice.setPetOwner(owner);

        double totalAmount = 0.0;

        InvoiceItem visitItem = new InvoiceItem();
        visitItem.setDescription("Consultation: " + visit.getReason());
        visitItem.setQuantity(1);
        visitItem.setUnitPrice(visit.getPrice());
        visitItem.setItemTotal(visit.getPrice());
        visitItem.setInvoice(invoice);

        invoice.getItems().add(visitItem);
        totalAmount += visit.getPrice();

        if (visit.getTreatment() != null) {
            Treatment t = visit.getTreatment();
            InvoiceItem treatmentItem = new InvoiceItem();
            treatmentItem.setDescription("Treatment: " + t.getName());
            treatmentItem.setQuantity(1);
            treatmentItem.setUnitPrice(t.getCost());
            treatmentItem.setItemTotal(t.getCost());
            treatmentItem.setInvoice(invoice);

            invoice.getItems().add(treatmentItem);
            totalAmount += t.getCost();
        }
        invoice.setTotalAmount(totalAmount);

        applyLoyaltyDiscount(invoice, owner);

        Invoice saved = invoiceRepo.save(invoice);
        return MapperHelper.mapInvoiceDTO(saved);
    }

    @Transactional
    public InvoiceDTO applyDiscount(Long invoiceId, double manualPercentage) {

        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found: " + invoiceId));

        if ("PAID".equalsIgnoreCase(invoice.getStatus())) {
            throw new InvalidDataException("Cannot apply discount to a paid invoice");
        }

        double discountPercentage = 0.0;
        String discountDescription;

        if (manualPercentage > 0) {
            discountPercentage = manualPercentage;
            discountDescription = "Manual discount (" + manualPercentage + "%)";
        } else {
            throw new InvalidDataException("No discount code or percentage provided");
        }

        double discountAmount = (invoice.getTotalAmount() * discountPercentage) / 100.0;
        invoice.setDiscountAmount(discountAmount);
        invoice.setFinalAmount(invoice.getTotalAmount() - discountAmount);

        InvoiceItem discountItem = new InvoiceItem();
        discountItem.setDescription(discountDescription);
        discountItem.setQuantity(1);
        discountItem.setUnitPrice(-discountAmount);
        discountItem.setItemTotal(-discountAmount);
        discountItem.setInvoice(invoice);

        if (invoice.getItems() == null)
            invoice.setItems(new java.util.ArrayList<>());

        invoice.getItems().add(discountItem);

        Invoice saved = invoiceRepo.save(invoice);
        return MapperHelper.mapInvoiceDTO(saved);
    }

    @Transactional
    public InvoiceDTO processInvoicePayment(Long invoiceId, double amountPaid, PaymentMethod method) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        String status = invoice.getStatus();
        if (!"UNPAID".equalsIgnoreCase(status) && !"PARTIALLY_PAID".equalsIgnoreCase(status)) {
            throw new InvalidDataException("Invoice must be Unpaid or Partially Paid to process payment");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amountPaid);
        payment.setPaymentMethod(method);
        payment.setTransactionRef(System.currentTimeMillis());

        paymentRepo.save(payment);

        // ← AGREGAR ESTAS LÍNEAS
        if (invoice.getPayments() == null) {
            invoice.setPayments(new java.util.ArrayList<>());
        }
        invoice.getPayments().add(payment); // ← Agregar el pago a la lista en memoria

        double totalPaid = invoice.getPayments().stream()
                .mapToDouble(Payment::getAmount).sum();

        double finalAmount = invoice.getFinalAmount();

        if (totalPaid >= finalAmount) {
            invoice.setStatus("PAID");
            if (invoice.getPetOwner() != null) {
                earnLoyaltyPoints(invoice.getPetOwner(), invoice.getFinalAmount());
            }
        } else {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepo.save(invoice);
        return MapperHelper.mapInvoiceDTO(invoice);
    }
    @Transactional
    public InvoiceDTO createMedicationSale(List<MedicationSaleRequest> saleItems, Long petOwnerId) {

        if (saleItems == null || saleItems.isEmpty()) {
            throw new InvalidDataException("Sale must contain at least one medication");
        }

        PetOwner owner = null;
        if (petOwnerId != null) {
            owner = petOwnerRepo.findById(petOwnerId)
                    .orElseThrow(() -> new NotFoundException("PetOwner not found: " + petOwnerId));
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setStatus("UNPAID");
        invoice.setPetOwner(owner);

        double totalAmount = 0.0;

        for (MedicationSaleRequest req : saleItems) {

            Medication med = medicationRepo.findById(req.medicationId())
                    .orElseThrow(() -> new NotFoundException("Medication not found: " + req.medicationId()));

            if (req.quantity() <= 0) {
                throw new InvalidDataException("Quantity must be > 0");
            }

            double itemTotal = req.quantity() * med.getUnitPrice();

            InvoiceItem item = new InvoiceItem();
            item.setDescription("Medication sale: " + med.getName());
            item.setQuantity(req.quantity());
            item.setUnitPrice(med.getUnitPrice());
            item.setItemTotal(itemTotal);
            item.setInvoice(invoice);

            invoice.getItems().add(item);
            totalAmount += itemTotal;
        }

        invoice.setTotalAmount(totalAmount);

        applyLoyaltyDiscount(invoice, owner);

        Invoice saved = invoiceRepo.save(invoice);

        return MapperHelper.mapInvoiceDTO(saved);
    }
}
