package iuh.fit.orderservice.dto;

import java.util.UUID;

public record ProductSoldUpdateRequest(
        UUID productId,
        Integer quantity
) {
}