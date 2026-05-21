package iuh.fit.orderservice.voucher.controller;

import iuh.fit.orderservice.voucher.dto.VoucherRedemptionResponseDTO;
import iuh.fit.orderservice.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller cung cấp các API tra cứu lịch sử sử dụng voucher.
 */
@RestController
@RequestMapping("/api/v1/orders/voucher-redemptions")
@RequiredArgsConstructor
public class VoucherRedemptionController {

    /** Dịch vụ nghiệp vụ của voucher. */
    private final VoucherService voucherService;

    /**
     * Lấy chi tiết một lịch sử sử dụng voucher theo mã định danh.
     *
     * @param redemptionId mã định danh bản ghi lịch sử
     * @return bản ghi lịch sử sử dụng voucher tương ứng
     */
    @GetMapping("/{redemptionId}")
    public ResponseEntity<VoucherRedemptionResponseDTO> getRedemptionById(@PathVariable UUID redemptionId) {
        return ResponseEntity.ok(voucherService.getRedemptionById(redemptionId));
    }

    /**
     * Lấy danh sách lịch sử sử dụng theo voucher.
     *
     * @param voucherId mã định danh voucher
     * @return danh sách lịch sử sử dụng của voucher
     */
    @GetMapping("/voucher/{voucherId}")
    public ResponseEntity<List<VoucherRedemptionResponseDTO>> getRedemptionsByVoucherId(@PathVariable UUID voucherId) {
        return ResponseEntity.ok(voucherService.getRedemptionsByVoucherId(voucherId));
    }

    /**
     * Lấy danh sách lịch sử sử dụng theo khách hàng.
     *
     * @param customerId mã định danh khách hàng
     * @return danh sách lịch sử sử dụng của khách hàng
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<VoucherRedemptionResponseDTO>> getRedemptionsByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(voucherService.getRedemptionsByCustomerId(customerId));
    }
}