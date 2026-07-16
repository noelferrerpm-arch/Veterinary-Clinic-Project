package cat.tecnocampus.application.dto;

public record InvoiceItemDTO(
    long itemId,
    String description,
    int quantity,
    double unitPrice,
    double itemTotal
){
}
