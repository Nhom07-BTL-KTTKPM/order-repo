package iuh.fit.orderservice.voucher.entity;

/**
 * Trạng thái vòng đời của voucher trong hệ thống.
 */
public enum VoucherStatus {
    /** Voucher chưa đến thời điểm bắt đầu hiệu lực. */
    UPCOMING,

    /** Voucher đang có hiệu lực và có thể áp dụng. */
    ACTIVE,

    /** Voucher đã hết thời gian hiệu lực hoặc đã hết số lượng. */
    EXPIRED,

    /** Voucher bị vô hiệu hóa bởi quản trị viên. */
    DISABLED
}