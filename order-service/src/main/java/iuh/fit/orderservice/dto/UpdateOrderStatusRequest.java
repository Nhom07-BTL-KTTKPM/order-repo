package iuh.fit.orderservice.dto;

public record UpdateOrderStatusRequest(
        String status,
        String cancelReason,
        String paymentStatus
) {
}
