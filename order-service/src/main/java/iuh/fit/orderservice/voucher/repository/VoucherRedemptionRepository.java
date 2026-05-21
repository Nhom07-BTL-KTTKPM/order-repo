package iuh.fit.orderservice.voucher.repository;

import iuh.fit.orderservice.voucher.entity.VoucherRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu cho lịch sử sử dụng voucher.
 */
public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, UUID> {

    /**
     * Lấy toàn bộ lịch sử sử dụng theo voucher, sắp xếp mới nhất trước.
     *
     * @param voucherId mã định danh voucher
     * @return danh sách lịch sử sử dụng của voucher
     */
    List<VoucherRedemption> findByVoucherIdOrderByRedeemedAtDesc(UUID voucherId);

    /**
     * Lấy toàn bộ lịch sử sử dụng theo khách hàng, sắp xếp mới nhất trước.
     *
     * @param customerId mã định danh khách hàng
     * @return danh sách lịch sử sử dụng của khách hàng
     */
    List<VoucherRedemption> findByCustomerIdOrderByRedeemedAtDesc(UUID customerId);

    /**
     * Đếm số lần một khách hàng đã sử dụng một voucher.
     *
     * @param voucherId mã định danh voucher
     * @param customerId mã định danh khách hàng
     * @return số lần đã sử dụng voucher
     */
    long countByVoucherIdAndCustomerId(UUID voucherId, UUID customerId);
}