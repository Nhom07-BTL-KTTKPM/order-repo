package iuh.fit.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productVariantId,
        String productName,
        String variantName,
        String imageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
