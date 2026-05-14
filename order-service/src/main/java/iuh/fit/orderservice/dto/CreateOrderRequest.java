package iuh.fit.orderservice.dto;

public record CreateOrderRequest(
        String customerId,
        String recipientName,
        String shippingAddress,
        String email,
        String phone,
        String note,
        String paymentMethod,
        java.util.List<String> selectedItemIds
) {
}
