package iuh.fit.orderservice.voucher.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO chuẩn hóa thông tin lỗi trả về cho client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorMessage {

    /** Mã trạng thái HTTP của lỗi. */
    private int statusCode;

    /** Thời điểm lỗi được ghi nhận. */
    private LocalDateTime timestamp;

    /** Thông điệp mô tả lỗi chính. */
    private String message;

    /** Mô tả ngữ cảnh hoặc đường dẫn gây ra lỗi. */
    private String description;
}