package iuh.fit.orderservice.dto;

import java.util.List;
import java.util.UUID;

public record StockAdjustmentRequest(
        List<StockAdjustmentItem> items
) {
    public record StockAdjustmentItem(
            UUID productVariantId,
            Integer quantity
    ) {
    }
}
