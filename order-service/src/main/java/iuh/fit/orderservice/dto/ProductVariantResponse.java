package iuh.fit.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductVariantResponse(
        UUID id,
        UUID productId,
        String variantName,
        BigDecimal price,
        String imageUrl,
        Integer stockQuantity,
        Boolean isActive
) {
}
