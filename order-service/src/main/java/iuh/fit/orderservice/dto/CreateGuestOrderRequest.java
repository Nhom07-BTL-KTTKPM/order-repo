package iuh.fit.orderservice.dto;

import java.util.List;

public record CreateGuestOrderRequest(
        String recipientName,
        String shippingAddress,
        String email,
        String phone,
        String note,
        List<GuestOrderItem> items,
        String voucherCode,
        java.math.BigDecimal shippingFee
) {
}
