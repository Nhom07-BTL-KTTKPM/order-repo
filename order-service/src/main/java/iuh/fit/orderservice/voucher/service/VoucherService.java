package iuh.fit.orderservice.voucher.service;

import iuh.fit.orderservice.voucher.dto.VoucherRedemptionResponseDTO;
import iuh.fit.orderservice.voucher.dto.VoucherRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherResponseDTO;
import iuh.fit.orderservice.voucher.dto.VoucherStatusChangeRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherValidationRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherValidationResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Hợp đồng nghiệp vụ cho voucher và lịch sử sử dụng voucher.
 */
public interface VoucherService {

	/**
	 * Lấy toàn bộ danh sách voucher trong hệ thống.
	 *
	 * @return danh sách voucher đã được ánh xạ sang DTO
	 */
	List<VoucherResponseDTO> getAllVouchers();

	/**
	 * Lấy chi tiết voucher theo mã định danh.
	 *
	 * @param voucherId mã định danh voucher
	 * @return thông tin voucher tương ứng
	 */
	VoucherResponseDTO getVoucherById(UUID voucherId);

	/**
	 * Lấy chi tiết voucher theo mã code.
	 *
	 * @param code mã voucher cần tra cứu
	 * @return thông tin voucher tương ứng
	 */
	VoucherResponseDTO getVoucherByCode(String code);

	/**
	 * Tạo mới một voucher.
	 *
	 * @param request dữ liệu đầu vào cho voucher mới
	 * @return voucher vừa được tạo
	 */
	VoucherResponseDTO createVoucher(VoucherRequestDTO request);

	/**
	 * Cập nhật thông tin voucher.
	 *
	 * @param voucherId mã định danh voucher cần cập nhật
	 * @param request dữ liệu cập nhật
	 * @return voucher sau khi cập nhật
	 */
	VoucherResponseDTO updateVoucher(UUID voucherId, VoucherRequestDTO request);

	/**
	 * Xóa voucher khỏi hệ thống.
	 *
	 * @param voucherId mã định danh voucher cần xóa
	 */
	void deleteVoucher(UUID voucherId);

	/**
	 * Thay đổi trạng thái voucher.
	 *
	 * @param voucherId mã định danh voucher
	 * @param request trạng thái mới của voucher
	 * @return voucher sau khi thay đổi trạng thái
	 */
	VoucherResponseDTO changeVoucherStatus(UUID voucherId, VoucherStatusChangeRequestDTO request);

	/**
	 * Kiểm tra voucher có thể áp dụng cho đơn hàng hay không.
	 *
	 * @param voucherId mã định danh voucher
	 * @param request dữ liệu dùng để kiểm tra
	 * @return kết quả kiểm tra và số tiền giảm dự kiến
	 */
	VoucherValidationResponseDTO validateVoucher(UUID voucherId, VoucherValidationRequestDTO request);

	/**
	 * Ghi nhận một lần sử dụng voucher trong giao dịch checkout thành công.
	 *
	 * @param voucherId mã định danh voucher
	 * @param customerId mã định danh khách hàng
	 * @param orderId mã định danh đơn hàng
	 * @param orderAmount giá trị đơn hàng trước khi giảm
	 * @param shippingFee phí vận chuyển gốc của đơn hàng
	 * @return bản ghi lịch sử sử dụng voucher vừa được tạo
	 */
	VoucherRedemptionResponseDTO redeemVoucher(UUID voucherId, UUID customerId, UUID orderId, BigDecimal orderAmount, BigDecimal shippingFee);

	/**
	 * Lấy chi tiết một bản ghi lịch sử sử dụng voucher theo mã định danh.
	 *
	 * @param redemptionId mã định danh lịch sử sử dụng
	 * @return bản ghi lịch sử tương ứng
	 */
	VoucherRedemptionResponseDTO getRedemptionById(UUID redemptionId);

	/**
	 * Lấy danh sách lịch sử sử dụng theo voucher.
	 *
	 * @param voucherId mã định danh voucher
	 * @return danh sách lịch sử áp dụng voucher
	 */
	List<VoucherRedemptionResponseDTO> getRedemptionsByVoucherId(UUID voucherId);

	/**
	 * Lấy danh sách lịch sử sử dụng theo khách hàng.
	 *
	 * @param customerId mã định danh khách hàng
	 * @return danh sách lịch sử sử dụng của khách hàng
	 */
	List<VoucherRedemptionResponseDTO> getRedemptionsByCustomerId(UUID customerId);
}