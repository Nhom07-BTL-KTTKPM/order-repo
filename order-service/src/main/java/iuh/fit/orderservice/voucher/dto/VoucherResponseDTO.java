package iuh.fit.orderservice.voucher.dto;

import iuh.fit.orderservice.voucher.entity.VoucherStatus;
import iuh.fit.orderservice.voucher.entity.VoucherType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO phản hồi thông tin voucher cho các API CRUD.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponseDTO {

    /** Mã định danh voucher. */
    private UUID id;

    /** Mã voucher viết hoa, duy nhất trong hệ thống. */
    private String code;

    /** Tên chương trình khuyến mãi. */
    private String name;

    /** Mô tả chi tiết của voucher. */
    private String description;

    /** Loại voucher quyết định công thức tính giảm giá. */
    private VoucherType type;

    /** Giá trị giảm theo phần trăm hoặc số tiền cố định. */
    private BigDecimal discountValue;

    /** Mức giảm tối đa được phép áp dụng. */
    private BigDecimal maxDiscountAmount;

    /** Giá trị đơn hàng tối thiểu để voucher có hiệu lực. */
    private BigDecimal minOrderAmount;

    /** Số lượng voucher còn lại có thể được sử dụng. */
    private Integer quantity;

    /** Số lần sử dụng tối đa trên mỗi khách hàng. */
    private Integer maxUsagePerUser;

    /** Trạng thái hiện tại của voucher. */
    private VoucherStatus status;

    /** Thời điểm voucher bắt đầu có hiệu lực. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /** Thời điểm voucher hết hiệu lực. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /** Thời điểm voucher được tạo. */
    private LocalDateTime createdAt;
}