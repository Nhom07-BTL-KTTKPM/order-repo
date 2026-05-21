package iuh.fit.orderservice.voucher.exception;

/**
 * Ngoại lệ được ném ra khi voucher hoặc lịch sử sử dụng voucher không tồn tại.
 */
public class VoucherNotFoundException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông điệp chi tiết.
     *
     * @param message thông điệp mô tả lỗi
     */
    public VoucherNotFoundException(String message) {
        super(message);
    }
}