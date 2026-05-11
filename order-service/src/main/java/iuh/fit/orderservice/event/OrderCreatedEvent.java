package iuh.fit.orderservice.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        String orderCode,
        List<OrderCreatedItem> items
) {
    public record OrderCreatedItem(
            UUID productVariantId,
            Integer quantity
    ) {
    }
}
