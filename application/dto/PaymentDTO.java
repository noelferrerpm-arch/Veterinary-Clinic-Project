package cat.tecnocampus.application.dto;

import cat.tecnocampus.domain.PaymentMethod;

import java.time.LocalDate;

public record PaymentDTO(
        Long paymentId,
        LocalDate paymentDate,
        double amount,
        PaymentMethod paymentMethod,
        Long transactionRef
) {}
