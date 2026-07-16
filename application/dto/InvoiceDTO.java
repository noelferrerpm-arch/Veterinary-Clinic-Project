package cat.tecnocampus.application.dto;

import cat.tecnocampus.application.dto.InvoiceItemDTO;
import cat.tecnocampus.application.dto.PaymentDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record InvoiceDTO(
        Long invoiceId,
        LocalDate invoiceDate,
        double totalAmount,
        double discountAmount,
        double finalAmount,
        String status,
        Long petOwnerId,
        List<InvoiceItemDTO> items,
        List<PaymentDTO> payments,
        Set<Long> discountIds
) {}