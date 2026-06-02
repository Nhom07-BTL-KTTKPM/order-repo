package iuh.fit.orderservice.voucher.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Thực thể lưu thông tin mã giảm giá trong hệ thống.
 */
@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    /** Khóa chính định danh duy nhất của voucher. */
    @Id
    @UuidGenerator
    private UUID id;

    /** Mã voucher duy nhất, được lưu ở dạng chữ in hoa. */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /** Tên chương trình khuyến mãi. */
    @Column(nullable = false, length = 255)
    private String name;

    /** Mô tả chi tiết của chương trình khuyến mãi. */
    @Column(length = 2000)
    private String description;

    /** Loại voucher để xác định cách tính tiền giảm. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VoucherType type;

    /** Giá trị giảm theo phần trăm hoặc số tiền cố định tùy theo loại voucher. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    /** Số tiền giảm tối đa đối với voucher giảm theo phần trăm. */
    @Column(precision = 19, scale = 2)
    private BigDecimal maxDiscountAmount;

    /** Giá trị đơn hàng tối thiểu để voucher có thể được áp dụng. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minOrderAmount;

    /** Số lượng voucher còn lại có thể được sử dụng. */
    @Column(nullable = false)
    private Integer quantity;

    /** Giới hạn số lần sử dụng trên mỗi khách hàng, cho phép để trống nếu không giới hạn. */
    @Column
    private Integer maxUsagePerUser;

    /** Trạng thái hiện tại của voucher. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VoucherStatus status;

    /** Thời điểm voucher bắt đầu có hiệu lực. */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /** Thời điểm voucher hết hiệu lực. */
    @Column(nullable = false)
    private LocalDateTime endDate;

    /** Thời điểm voucher được tạo trong hệ thống. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Danh sách lịch sử sử dụng voucher được liên kết với mã giảm giá này. */
    @Builder.Default
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VoucherRedemption> redemptions = new ArrayList<>();
}