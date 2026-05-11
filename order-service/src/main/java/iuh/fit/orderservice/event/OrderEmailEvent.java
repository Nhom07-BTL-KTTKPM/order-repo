package iuh.fit.orderservice.event;

import java.math.BigDecimal;

public record OrderEmailEvent(
        String email,
        String fullName,
        String orderCode,
        String status,
        BigDecimal totalAmount,
        String updatedAt
) {
}
