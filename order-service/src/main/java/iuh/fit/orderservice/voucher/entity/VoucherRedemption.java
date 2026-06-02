package iuh.fit.orderservice.voucher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Thực thể lưu lịch sử mỗi lần voucher được sử dụng.
 */
@Entity
@Table(name = "voucher_redemptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherRedemption {

    /** Khóa chính định danh duy nhất của bản ghi sử dụng voucher. */
    @Id
    @UuidGenerator
    private UUID id;

    /** Voucher được áp dụng cho lần sử dụng này. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    /** Mã định danh khách hàng đã sử dụng voucher, lưu thuần UUID. */
    @Column(nullable = false)
    private UUID customerId;

    /** Mã định danh đơn hàng đã áp dụng voucher, lưu thuần UUID. */
    @Column(nullable = false)
    private UUID orderId;

    /** Số tiền thực tế đã được giảm sau khi áp dụng voucher. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountDiscounted;

    /** Thời điểm voucher được áp dụng. */
    @Column(nullable = false)
    private LocalDateTime redeemedAt;
}