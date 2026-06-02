package iuh.fit.orderservice.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO phản hồi kết quả kiểm tra khả năng áp dụng voucher.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherValidationResponseDTO {

    /** Mã định danh voucher đã được kiểm tra. */
    private UUID voucherId;

    /** Mã code của voucher đã được kiểm tra. */
    private String code;

    /** Kết quả xác nhận voucher có thể áp dụng hay không. */
    private boolean valid;

    /** Thông điệp giải thích lý do hợp lệ hoặc không hợp lệ. */
    private String message;

    /** Số tiền dự kiến sẽ được giảm nếu voucher được áp dụng. */
    private BigDecimal discountAmount;

    /** Số lượng voucher còn lại trong hệ thống tại thời điểm kiểm tra. */
    private Integer remainingQuantity;
}