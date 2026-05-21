package iuh.fit.orderservice.dto;

import iuh.fit.orderservice.entity.OrderStatus;
import iuh.fit.orderservice.entity.PaymentMethod;
import iuh.fit.orderservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderCode,
        UUID customerId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal total,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String recipientName,
        String shippingAddress,
        String email,
        String phone,
        String note,
        LocalDateTime orderDate,
        LocalDateTime updatedAt,
        String cancelReason,
        List<OrderItemResponse> items
) {
}
