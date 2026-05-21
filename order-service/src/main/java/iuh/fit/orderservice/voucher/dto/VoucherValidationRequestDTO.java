package iuh.fit.orderservice.voucher.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO dùng để kiểm tra voucher có thể được áp dụng cho một đơn hàng hay không.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherValidationRequestDTO {

    /** Mã định danh khách hàng dùng để kiểm tra giới hạn theo người dùng, có thể để trống nếu voucher không giới hạn theo người dùng. */
    private UUID customerId;

    /** Giá trị tạm tính của đơn hàng trước khi áp dụng voucher. */
    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @PositiveOrZero(message = "Giá trị đơn hàng phải lớn hơn hoặc bằng 0")
    private BigDecimal orderAmount;

    /** Phí vận chuyển gốc của đơn hàng, dùng cho voucher miễn phí vận chuyển. */
    @PositiveOrZero(message = "Phí vận chuyển phải lớn hơn hoặc bằng 0")
    private BigDecimal shippingFee;
}