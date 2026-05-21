package iuh.fit.orderservice.voucher.controller;

import iuh.fit.orderservice.voucher.dto.VoucherRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherResponseDTO;
import iuh.fit.orderservice.voucher.dto.VoucherStatusChangeRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherValidationRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherValidationResponseDTO;
import iuh.fit.orderservice.voucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller cung cấp các API quản lý voucher.
 */
@RestController
@RequestMapping("/api/v1/orders/voucher")
@RequiredArgsConstructor
public class VoucherController {

    /** Dịch vụ nghiệp vụ của voucher. */
    private final VoucherService voucherService;

    /**
     * Lấy toàn bộ danh sách voucher.
     *
     * @return danh sách voucher trong hệ thống
     */
    @GetMapping
    public ResponseEntity<List<VoucherResponseDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    /**
     * Lấy chi tiết voucher theo mã định danh.
     *
     * @param voucherId mã định danh voucher
     * @return thông tin voucher tương ứng
     */
    @GetMapping("/{voucherId}")
    public ResponseEntity<VoucherResponseDTO> getVoucherById(@PathVariable UUID voucherId) {
        return ResponseEntity.ok(voucherService.getVoucherById(voucherId));
    }

    /**
     * Lấy chi tiết voucher theo mã code.
     *
     * @param code mã voucher cần tra cứu
     * @return thông tin voucher tương ứng
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<VoucherResponseDTO> getVoucherByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }

    /**
     * Tạo mới voucher.
     *
     * @param request dữ liệu voucher cần tạo
     * @return voucher vừa được tạo
     */
    @PostMapping
    public ResponseEntity<VoucherResponseDTO> createVoucher(@Valid @RequestBody VoucherRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(request));
    }

    /**
     * Cập nhật thông tin voucher.
     *
     * @param voucherId mã định danh voucher
     * @param request dữ liệu cập nhật voucher
     * @return voucher sau khi cập nhật
     */
    @PutMapping("/{voucherId}")
    public ResponseEntity<VoucherResponseDTO> updateVoucher(@PathVariable UUID voucherId, @Valid @RequestBody VoucherRequestDTO request) {
        return ResponseEntity.ok(voucherService.updateVoucher(voucherId, request));
    }

    /**
     * Xóa voucher khỏi hệ thống.
     *
     * @param voucherId mã định danh voucher
     * @return phản hồi không nội dung
     */
    @DeleteMapping("/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable UUID voucherId) {
        voucherService.deleteVoucher(voucherId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Thay đổi trạng thái của voucher.
     *
     * @param voucherId mã định danh voucher
     * @param request trạng thái mới của voucher
     * @return voucher sau khi đổi trạng thái
     */
    @PatchMapping("/{voucherId}/status")
    public ResponseEntity<VoucherResponseDTO> changeVoucherStatus(
            @PathVariable UUID voucherId,
            @Valid @RequestBody VoucherStatusChangeRequestDTO request) {
        return ResponseEntity.ok(voucherService.changeVoucherStatus(voucherId, request));
    }

    /**
     * Kiểm tra voucher có thể được áp dụng cho đơn hàng hay không.
     *
     * @param voucherId mã định danh voucher
     * @param request dữ liệu đơn hàng dùng để kiểm tra
     * @return kết quả kiểm tra voucher
     */
    @PostMapping("/{voucherId}/validate")
    public ResponseEntity<VoucherValidationResponseDTO> validateVoucher(
            @PathVariable UUID voucherId,
            @Valid @RequestBody VoucherValidationRequestDTO request) {
        return ResponseEntity.ok(voucherService.validateVoucher(voucherId, request));
    }

    /**
     * Lấy danh sách voucher phục vụ kiểm thử nhanh trên gateway.
     *
     * @return danh sách voucher trong hệ thống
     */
    @GetMapping("/test")
    public ResponseEntity<List<VoucherResponseDTO>> testGetVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    /**
     * Kiểm thử điều kiện áp dụng voucher theo mã định danh.
     *
     * @param voucherId mã định danh voucher
     * @param request dữ liệu đơn hàng dùng để kiểm tra
     * @return kết quả kiểm tra voucher
     */
    @PostMapping("/test/{voucherId}/validate")
    public ResponseEntity<VoucherValidationResponseDTO> testValidateVoucher(
            @PathVariable UUID voucherId,
            @Valid @RequestBody VoucherValidationRequestDTO request) {
        return ResponseEntity.ok(voucherService.validateVoucher(voucherId, request));
    }
}