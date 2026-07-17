package cat.tecnocampus.service;

import cat.tecnocampus.application.dto.InvoiceDTO;
import cat.tecnocampus.application.dto.InvoiceItemDTO;
import cat.tecnocampus.application.dto.PaymentDTO;
import cat.tecnocampus.application.dto.MedicationSaleRequest;
import cat.tecnocampus.application.service.InvoiceService;
import cat.tecnocampus.domain.PaymentMethod;
import cat.tecnocampus.domain.Status_VPR;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import cat.tecnocampus.persistence.VisitRepository;
import cat.tecnocampus.persistence.MedicationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class InvoiceServiceTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    // 1. Crear factura simple (sin propietario) y comprobar campos básicos
    @Test
    public void createInvoice_basic() {
        double total = 50.0;
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "Service A", 1, 50.0, 50.0);
        PaymentDTO payment = new PaymentDTO(null, LocalDate.now(), 50.0, PaymentMethod.CASH, 1L);

        InvoiceDTO dto = new InvoiceDTO(
                null,
                null,
                total,
                0.0,
                total,
                "UNPAID",
                null,
                List.of(item),
                List.of(payment),
                Set.of()
        );

        InvoiceDTO saved = invoiceService.createInvoice(dto);
        Assertions.assertNotNull(saved.invoiceId());
        Assertions.assertEquals(total, saved.totalAmount());
        Assertions.assertEquals("UNPAID", saved.status());
    }

    // 2. Actualizar factura
    @Test
    public void updateInvoice_changesAmountAndStatus() {
        // crear
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "X", 1, 30.0, 30.0);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 30.0, 0.0, 30.0, "UNPAID", null, List.of(item), List.of(), Set.of()));

        // actualizar
        InvoiceDTO update = new InvoiceDTO(null, null, 100.0, 0.0, 100.0, "DRAFT", null, List.of(item), List.of(), Set.of());
        InvoiceDTO updated = invoiceService.updateInvoice(created.invoiceId(), update);

        Assertions.assertEquals(100.0, updated.totalAmount());
        Assertions.assertEquals("DRAFT", updated.status());
    }

    // 3. getInvoice y listAll
    @Test
    public void getAndListAll_containsCreated() {
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "Y", 1, 10.0, 10.0);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 10.0, 0.0, 10.0, "UNPAID", null, List.of(item), List.of(), Set.of()));

        InvoiceDTO fetched = invoiceService.getInvoice(created.invoiceId());
        Assertions.assertEquals(created.invoiceId(), fetched.invoiceId());

        List<InvoiceDTO> all = invoiceService.listAll();
        Assertions.assertTrue(all.stream().anyMatch(i -> i.invoiceId().equals(created.invoiceId())));
    }

    // 4. deleteInvoice sin pagos -> debe borrar
    @Test
    public void deleteInvoice_withoutPayments_success() {
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "Z", 1, 5.0, 5.0);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 5.0, 0.0, 5.0, "UNPAID", null, List.of(item), List.of(), Set.of()));

        invoiceService.deleteInvoice(created.invoiceId());
        Assertions.assertThrows(NotFoundException.class, () -> invoiceService.getInvoice(created.invoiceId()));
    }

    // 5. deleteInvoice con pagos -> debe lanzar InvalidDataException
    @Test
    public void deleteInvoice_withPayments_throws() {
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "P", 1, 20.0, 20.0);
        PaymentDTO payment = new PaymentDTO(null, LocalDate.now(), 20.0, PaymentMethod.CARD, 2L);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 20.0, 0.0, 20.0, "UNPAID", null, List.of(item), List.of(payment), Set.of()));

        Assertions.assertThrows(InvalidDataException.class, () -> invoiceService.deleteInvoice(created.invoiceId()));
    }

    // 6. markInvoiceAsPaid: crear factura con pagos que cubran el finalAmount y marcar como pagada
    @Test
    public void markInvoiceAsPaid_success() {
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "Srv", 1, 40.0, 40.0);
        PaymentDTO payment = new PaymentDTO(null, LocalDate.now(), 40.0, PaymentMethod.CARD, 3L);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 40.0, 0.0, 40.0, "UNPAID", null, List.of(item), List.of(payment), Set.of()));

        InvoiceDTO paid = invoiceService.markInvoiceAsPaid(created.invoiceId());
        Assertions.assertEquals("PAID", paid.status().toUpperCase());
    }

    // 7. generateInvoiceFromVisit: tomar un visit existente, marcar como Completed y generar factura
    @Test
    public void generateInvoiceFromVisit_completedVisit_createsInvoice() {
        // hay visitas en data.sql; se toma una (id 1 suele existir). Si no existe, falla la prueba.
        var visitOpt = visitRepository.findById(1L);
        if (visitOpt.isEmpty()) {
            // crear/guardar una visita no es posible si la entidad necesita muchos campos; en tal caso la prueba declarará NotFound
            Assertions.fail("Visit con id 1 no encontrada en la DB de pruebas (data.sql esperado).");
            return;
        }
        var visit = visitOpt.get();
        visit.setStatus(Status_VPR.Completed);
        visitRepository.save(visit);

        InvoiceDTO invoiceFromVisit = invoiceService.generateInvoiceFromVisit(visit.getVisitId());
        Assertions.assertNotNull(invoiceFromVisit);
        Assertions.assertTrue(invoiceFromVisit.totalAmount() > 0.0);
    }

    // 8. applyDiscount: aplicar descuento manual
    @Test
    public void applyDiscount_manualPercentage_changesFinalAmountAndAddsItem() {
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "ServiceD", 1, 100.0, 100.0);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 100.0, 0.0, 100.0, "UNPAID", null, List.of(item), List.of(), Set.of()));

        InvoiceDTO discounted = invoiceService.applyDiscount(created.invoiceId(), 10.0); // 10%
        Assertions.assertEquals(100.0 - 10.0, discounted.finalAmount(), 0.0001);
        // debería existir un item con unitPrice negativo (descuento)
        boolean hasDiscountItem = discounted.items().stream().anyMatch(i -> i.itemTotal() < 0);
        Assertions.assertTrue(hasDiscountItem);
    }

    // 9. processInvoicePayment: parcial y luego completo
    @Test
    public void processInvoicePayment_partialThenFull_changesStatus() {
        InvoiceItemDTO item = new InvoiceItemDTO(0L, "SrvP", 1, 100.0, 100.0);
        InvoiceDTO created = invoiceService.createInvoice(new InvoiceDTO(null, null, 100.0, 0.0, 100.0, "UNPAID", null, List.of(item), List.of(), Set.of()));

        InvoiceDTO partial = invoiceService.processInvoicePayment(created.invoiceId(), 40.0, PaymentMethod.CASH);
        Assertions.assertEquals("PARTIALLY_PAID", partial.status().toUpperCase());

        InvoiceDTO full = invoiceService.processInvoicePayment(created.invoiceId(), 60.0, PaymentMethod.CARD);
        Assertions.assertEquals("PAID", full.status().toUpperCase());
    }

    // 10. createMedicationSale: crear venta de medicamento usando ids del data.sql
    @Test
    public void createMedicationSale_validItems_createsInvoice() {
        // asume que existe medication con id 1 en data.sql
        Assertions.assertTrue(medicationRepository.findById(1L).isPresent(), "Se espera medication id 1 en data.sql");

        MedicationSaleRequest req = new MedicationSaleRequest(1L, 2);
        InvoiceDTO saleInvoice = invoiceService.createMedicationSale(List.of(req), 2L); // propietario id 2 existe en data.sql

        Assertions.assertNotNull(saleInvoice.invoiceId());
        Assertions.assertTrue(saleInvoice.totalAmount() > 0.0);
        Assertions.assertEquals(saleInvoice.items().size(), 1);
    }

    // 11. createMedicationSale con cantidad inválida -> InvalidDataException
    @Test
    public void createMedicationSale_invalidQuantity_throws() {
        MedicationSaleRequest bad = new MedicationSaleRequest(1L, 0);
        Assertions.assertThrows(InvalidDataException.class, () -> invoiceService.createMedicationSale(List.of(bad), null));
    }
}
