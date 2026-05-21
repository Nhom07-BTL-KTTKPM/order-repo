package iuh.fit.orderservice.voucher.entity;

/**
 * Loại khuyến mãi được áp dụng cho voucher.
 */
public enum VoucherType {
    /** Giảm theo phần trăm trên giá trị đơn hàng. */
    PERCENT,

    /** Giảm trừ một số tiền cố định trên đơn hàng. */
    AMOUNT,

    /** Miễn hoặc hỗ trợ phí vận chuyển cho đơn hàng. */
    FREE_SHIPPING
}