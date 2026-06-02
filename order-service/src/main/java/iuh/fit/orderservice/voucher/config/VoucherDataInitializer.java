package iuh.fit.orderservice.voucher.config;

import iuh.fit.orderservice.voucher.entity.Voucher;
import iuh.fit.orderservice.voucher.entity.VoucherStatus;
import iuh.fit.orderservice.voucher.entity.VoucherType;
import iuh.fit.orderservice.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Khởi tạo dữ liệu voucher mẫu phục vụ kiểm thử hệ thống khi ứng dụng khởi động.
 */
@Component
@RequiredArgsConstructor
public class VoucherDataInitializer implements CommandLineRunner {

    /** Repository quản lý dữ liệu voucher. */
    private final VoucherRepository voucherRepository;

    /**
     * Tạo dữ liệu mẫu nếu hệ thống chưa có voucher nào.
     *
     * @param args tham số dòng lệnh khi khởi động ứng dụng
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (voucherRepository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = List.of(
                Voucher.builder()
                        .code("TEST-PERCENT-ACTIVE")
                        .name("Voucher giảm theo phần trăm đang hoạt động")
                        .description("Voucher kiểm thử cho loại PERCENT với trạng thái ACTIVE")
                        .type(VoucherType.PERCENT)
                        .discountValue(BigDecimal.valueOf(10))
                        .maxDiscountAmount(BigDecimal.valueOf(50000))
                        .minOrderAmount(BigDecimal.valueOf(100000))
                        .quantity(100)
                        .maxUsagePerUser(1)
                        .status(VoucherStatus.ACTIVE)
                        .startDate(now.minusDays(1))
                        .endDate(now.plusMonths(6))
                        .createdAt(now)
                        .build(),
                Voucher.builder()
                        .code("TEST-AMOUNT-ACTIVE")
                        .name("Voucher giảm số tiền cố định đang hoạt động")
                        .description("Voucher kiểm thử cho loại AMOUNT với trạng thái ACTIVE")
                        .type(VoucherType.AMOUNT)
                        .discountValue(BigDecimal.valueOf(20000))
                        .maxDiscountAmount(BigDecimal.valueOf(20000))
                        .minOrderAmount(BigDecimal.valueOf(50000))
                        .quantity(50)
                        .maxUsagePerUser(null)
                        .status(VoucherStatus.ACTIVE)
                        .startDate(now.minusDays(1))
                        .endDate(now.plusMonths(6))
                        .createdAt(now)
                        .build(),
                Voucher.builder()
                        .code("TEST-FREE-SHIPPING-ACTIVE")
                        .name("Voucher miễn phí vận chuyển đang hoạt động")
                        .description("Voucher kiểm thử cho loại FREE_SHIPPING với trạng thái ACTIVE")
                        .type(VoucherType.FREE_SHIPPING)
                        .discountValue(BigDecimal.valueOf(15000))
                        .maxDiscountAmount(BigDecimal.valueOf(15000))
                        .minOrderAmount(BigDecimal.ZERO)
                        .quantity(30)
                        .maxUsagePerUser(2)
                        .status(VoucherStatus.ACTIVE)
                        .startDate(now.minusDays(1))
                        .endDate(now.plusMonths(6))
                        .createdAt(now)
                        .build(),
                Voucher.builder()
                        .code("TEST-PERCENT-UPCOMING")
                        .name("Voucher giảm theo phần trăm sắp hiệu lực")
                        .description("Voucher kiểm thử cho loại PERCENT với trạng thái UPCOMING")
                        .type(VoucherType.PERCENT)
                        .discountValue(BigDecimal.valueOf(15))
                        .maxDiscountAmount(BigDecimal.valueOf(100000))
                        .minOrderAmount(BigDecimal.valueOf(200000))
                        .quantity(200)
                        .maxUsagePerUser(null)
                        .status(VoucherStatus.UPCOMING)
                        .startDate(now.plusDays(1))
                        .endDate(now.plusMonths(7))
                        .createdAt(now)
                        .build()
        );

        voucherRepository.saveAll(vouchers);
    }
}