package iuh.fit.orderservice.voucher.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Bộ xử lý ngoại lệ tập trung cho module Voucher.
 */
@RestControllerAdvice(basePackages = "iuh.fit.orderservice.voucher")
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation dữ liệu đầu vào từ request body.
     *
     * @param exception ngoại lệ validation được Spring ném ra
     * @return phản hồi lỗi đã chuẩn hóa
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, "Validation failed");
    }

    /**
     * Xử lý lỗi ràng buộc validation ở mức tham số hoặc điều kiện bổ sung.
     *
     * @param exception ngoại lệ ràng buộc validation
     * @return phản hồi lỗi đã chuẩn hóa
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolation(ConstraintViolationException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), "Validation constraint violation");
    }

    /**
     * Xử lý lỗi dữ liệu đầu vào không hợp lệ.
     *
     * @param exception ngoại lệ dữ liệu đầu vào
     * @return phản hồi lỗi đã chuẩn hóa
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), "Illegal argument");
    }

    /**
     * Xử lý lỗi trạng thái nghiệp vụ không hợp lệ.
     *
     * @param exception ngoại lệ trạng thái nghiệp vụ
     * @return phản hồi lỗi đã chuẩn hóa
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorMessage> handleIllegalState(IllegalStateException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), "Illegal state");
    }

    /**
     * Xử lý lỗi không tìm thấy voucher hoặc redemption.
     *
     * @param exception ngoại lệ không tìm thấy dữ liệu
     * @return phản hồi lỗi đã chuẩn hóa
     */
    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleVoucherNotFound(VoucherNotFoundException exception) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), "Resource not found");
    }

    /**
     * Xử lý mọi ngoại lệ chưa được dự đoán trước.
     *
     * @param exception ngoại lệ hệ thống
     * @return phản hồi lỗi đã chuẩn hóa
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleUnexpectedException(Exception exception) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), "Unexpected server error");
    }

    /**
     * Tạo cấu trúc lỗi chuẩn để trả về client.
     *
     * @param status trạng thái HTTP tương ứng với lỗi
     * @param message thông điệp lỗi
     * @param description mô tả ngữ cảnh gây lỗi
     * @return phản hồi lỗi đã được chuẩn hóa
     */
    private ResponseEntity<ErrorMessage> buildErrorResponse(HttpStatus status, String message, String description) {
        ErrorMessage errorMessage = ErrorMessage.builder()
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .message(message)
                .description(description)
                .build();
        return ResponseEntity.status(status).body(errorMessage);
    }
}