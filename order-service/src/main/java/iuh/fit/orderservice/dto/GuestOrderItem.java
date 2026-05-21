package iuh.fit.orderservice.dto;

import java.util.UUID;

public record GuestOrderItem(UUID productVariantId, int quantity) {
}
