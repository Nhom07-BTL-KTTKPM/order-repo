package iuh.fit.orderservice.voucher.dto;

import iuh.fit.orderservice.voucher.entity.VoucherStatus;
import iuh.fit.orderservice.voucher.entity.VoucherType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng cho việc tạo mới và cập nhật voucher.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherRequestDTO {

    /** Mã voucher, bắt buộc viết hoa và không được trùng. */
    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 100, message = "Mã voucher không được vượt quá 100 ký tự")
    @jakarta.validation.constraints.Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã voucher phải ở dạng chữ in hoa, số, dấu gạch dưới hoặc gạch ngang")
    private String code;

    /** Tên chương trình khuyến mãi. */
    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 255, message = "Tên voucher không được vượt quá 255 ký tự")
    private String name;

    /** Mô tả chi tiết của chương trình khuyến mãi. */
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    /** Loại voucher quyết định cách tính tiền giảm. */
    @NotNull(message = "Loại voucher không được để trống")
    private VoucherType type;

    /** Giá trị giảm theo phần trăm hoặc số tiền cố định. */
    @NotNull(message = "Giá trị giảm không được để trống")
    @Positive(message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    /** Số tiền giảm tối đa đối với voucher giảm theo phần trăm, cho phép để trống nếu không giới hạn. */
    @PositiveOrZero(message = "Số tiền giảm tối đa phải lớn hơn hoặc bằng 0")
    private BigDecimal maxDiscountAmount;

    /** Giá trị đơn hàng tối thiểu để voucher được áp dụng. */
    @NotNull(message = "Giá trị đơn hàng tối thiểu không được để trống")
    @PositiveOrZero(message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal minOrderAmount;

    /** Số lượng voucher còn lại có thể sử dụng. */
    @NotNull(message = "Số lượng voucher không được để trống")
    @Positive(message = "Số lượng voucher phải lớn hơn 0")
    private Integer quantity;

    /** Số lần sử dụng tối đa trên mỗi khách hàng, có thể để trống nếu không giới hạn. */
    @Positive(message = "Số lần sử dụng tối đa phải lớn hơn 0")
    private Integer maxUsagePerUser;

    /** Trạng thái hiện tại của voucher. */
    @NotNull(message = "Trạng thái voucher không được để trống")
    private VoucherStatus status;

    /** Thời điểm voucher bắt đầu có hiệu lực. */
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    /** Thời điểm voucher hết hiệu lực. */
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;
}