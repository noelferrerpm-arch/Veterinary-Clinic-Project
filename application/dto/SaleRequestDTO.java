package cat.tecnocampus.application.dto;
import java.util.List;

public record SaleRequestDTO(
        Long petOwnerId,
        List<SaleItemDTO> items
) {}