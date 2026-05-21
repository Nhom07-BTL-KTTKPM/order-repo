package iuh.fit.orderservice.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO phản hồi thông tin lịch sử sử dụng voucher.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherRedemptionResponseDTO {

    /** Mã định danh của bản ghi lịch sử sử dụng voucher. */
    private UUID id;

    /** Mã định danh voucher đã được áp dụng. */
    private UUID voucherId;

    /** Mã định danh khách hàng đã sử dụng voucher. */
    private UUID customerId;

    /** Mã định danh đơn hàng đã áp dụng voucher. */
    private UUID orderId;

    /** Số tiền thực tế đã được giảm. */
    private BigDecimal amountDiscounted;

    /** Thời điểm voucher được áp dụng. */
    private LocalDateTime redeemedAt;
}