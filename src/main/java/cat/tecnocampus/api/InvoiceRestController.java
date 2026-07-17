package cat.tecnocampus.api;
import cat.tecnocampus.application.dto.InvoiceDTO;
import cat.tecnocampus.application.dto.MedicationDTO;
import cat.tecnocampus.application.dto.MedicationSaleRequest;
import cat.tecnocampus.application.service.InvoiceService;
import cat.tecnocampus.domain.PaymentMethod;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceRestController {

    private final InvoiceService invoiceService;

    public InvoiceRestController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDTO createInvoice(@RequestBody @Valid InvoiceDTO invoiceDTO){
        return this.invoiceService.createInvoice(invoiceDTO);
    }

    @GetMapping("/{id}")
    public InvoiceDTO getInvoice(@PathVariable Long id) {
        return invoiceService.getInvoice(id);
    }

    @GetMapping
    public List<InvoiceDTO> listInvoices() {
        return invoiceService.listAll();
    }

    @PutMapping("/{id}")
    public InvoiceDTO updateInvoice(@PathVariable Long id, @RequestBody @Valid InvoiceDTO dto) {
        return invoiceService.updateInvoice(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
    }

    @PatchMapping("/{id}/mark-paid")
    public InvoiceDTO markInvoiceAsPaid(@PathVariable Long id) {
        return invoiceService.markInvoiceAsPaid(id);
    }

    @PostMapping("/from-visit/{visitId}")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDTO generateInvoiceFromVisit(@PathVariable Long visitId) {
        return invoiceService.generateInvoiceFromVisit(visitId);
    }

    @PatchMapping("/{id}/apply-discount")
    public InvoiceDTO applyManualDiscount(@PathVariable Long id, @RequestParam(required = false, defaultValue = "0") double percentage) {
        return invoiceService.applyDiscount(id, percentage);
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDTO processPayment(@PathVariable Long id, @RequestParam double amountPaid, @RequestParam PaymentMethod paymentMethod) {
        return invoiceService.processInvoicePayment(id, amountPaid, paymentMethod);
    }

    @PostMapping("/medication-sale")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceDTO createMedicationSale(@RequestBody List<MedicationSaleRequest> saleItems, @RequestParam(required = false) Long petOwnerId) {
        return invoiceService.createMedicationSale(saleItems, petOwnerId);
    }
}
