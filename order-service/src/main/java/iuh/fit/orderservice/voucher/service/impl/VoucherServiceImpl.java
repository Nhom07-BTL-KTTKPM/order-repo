package iuh.fit.orderservice.voucher.service.impl;

import iuh.fit.orderservice.voucher.dto.VoucherRedemptionResponseDTO;
import iuh.fit.orderservice.voucher.dto.VoucherRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherResponseDTO;
import iuh.fit.orderservice.voucher.dto.VoucherStatusChangeRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherValidationRequestDTO;
import iuh.fit.orderservice.voucher.dto.VoucherValidationResponseDTO;
import iuh.fit.orderservice.voucher.entity.Voucher;
import iuh.fit.orderservice.voucher.entity.VoucherRedemption;
import iuh.fit.orderservice.voucher.entity.VoucherStatus;
import iuh.fit.orderservice.voucher.entity.VoucherType;
import iuh.fit.orderservice.voucher.exception.VoucherNotFoundException;
import iuh.fit.orderservice.voucher.repository.VoucherRedemptionRepository;
import iuh.fit.orderservice.voucher.repository.VoucherRepository;
import iuh.fit.orderservice.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ voucher và lịch sử sử dụng voucher.
 */
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    /** Quy tắc làm tròn tiền tệ áp dụng cho toàn bộ module voucher. */
    private static final RoundingMode CURRENCY_ROUNDING = RoundingMode.HALF_UP;

    private final VoucherRepository voucherRepository;
    private final VoucherRedemptionRepository voucherRedemptionRepository;

    /**
     * Lấy toàn bộ danh sách voucher trong hệ thống.
     *
     * @return danh sách voucher đã được ánh xạ sang DTO
     */
    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponseDTO> getAllVouchers() {
        return voucherRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Lấy chi tiết voucher theo mã định danh.
     *
     * @param voucherId mã định danh voucher
     * @return thông tin voucher tương ứng
     */
    @Override
    @Transactional(readOnly = true)
    public VoucherResponseDTO getVoucherById(UUID voucherId) {
        return mapToResponse(loadVoucher(voucherId));
    }

    /**
     * Lấy chi tiết voucher theo mã code.
     *
     * @param code mã voucher cần tra cứu
     * @return thông tin voucher tương ứng
     */
    @Override
    @Transactional(readOnly = true)
    public VoucherResponseDTO getVoucherByCode(String code) {
        return mapToResponse(loadVoucherByCode(code));
    }

    /**
     * Tạo mới một voucher.
     *
     * @param request dữ liệu đầu vào cho voucher mới
     * @return voucher vừa được tạo
     */
    @Override
    @Transactional
    public VoucherResponseDTO createVoucher(VoucherRequestDTO request) {
        validateVoucherRequest(request);

        String normalizedCode = normalizeCode(request.getCode());
        if (voucherRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại");
        }

        Voucher voucher = Voucher.builder()
                .code(normalizedCode)
                .name(request.getName().trim())
                .description(request.getDescription())
                .type(request.getType())
                .discountValue(normalizeCurrency(request.getDiscountValue()))
                .maxDiscountAmount(request.getMaxDiscountAmount() == null ? null : normalizeCurrency(request.getMaxDiscountAmount()))
                .minOrderAmount(normalizeCurrency(request.getMinOrderAmount()))
                .quantity(request.getQuantity())
                .maxUsagePerUser(request.getMaxUsagePerUser())
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(voucherRepository.save(voucher));
    }

    /**
     * Cập nhật thông tin voucher.
     *
     * @param voucherId mã định danh voucher cần cập nhật
     * @param request dữ liệu cập nhật
     * @return voucher sau khi cập nhật
     */
    @Override
    @Transactional
    public VoucherResponseDTO updateVoucher(UUID voucherId, VoucherRequestDTO request) {
        validateVoucherRequest(request);

        Voucher voucher = loadVoucher(voucherId);
        String normalizedCode = normalizeCode(request.getCode());
        if (voucherRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, voucherId)) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại");
        }

        voucher.setCode(normalizedCode);
        voucher.setName(request.getName().trim());
        voucher.setDescription(request.getDescription());
        voucher.setType(request.getType());
        voucher.setDiscountValue(normalizeCurrency(request.getDiscountValue()));
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount() == null ? null : normalizeCurrency(request.getMaxDiscountAmount()));
        voucher.setMinOrderAmount(normalizeCurrency(request.getMinOrderAmount()));
        voucher.setQuantity(request.getQuantity());
        voucher.setMaxUsagePerUser(request.getMaxUsagePerUser());
        voucher.setStatus(request.getStatus());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());

        return mapToResponse(voucherRepository.save(voucher));
    }

    /**
     * Vô hiệu hóa voucher thay vì xóa cứng khỏi hệ thống.
     *
     * @param voucherId mã định danh voucher cần xóa
     */
    @Override
    @Transactional
    public void deleteVoucher(UUID voucherId) {
        Voucher voucher = loadVoucher(voucherId);
        voucher.setStatus(VoucherStatus.DISABLED);
        voucherRepository.save(voucher);
    }

    /**
     * Thay đổi trạng thái voucher.
     *
     * @param voucherId mã định danh voucher
     * @param request trạng thái mới của voucher
     * @return voucher sau khi thay đổi trạng thái
     */
    @Override
    @Transactional
    public VoucherResponseDTO changeVoucherStatus(UUID voucherId, VoucherStatusChangeRequestDTO request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Trạng thái mới không được để trống");
        }

        Voucher voucher = loadVoucher(voucherId);
        voucher.setStatus(request.getStatus());
        return mapToResponse(voucherRepository.save(voucher));
    }

    /**
     * Kiểm tra voucher có thể áp dụng cho đơn hàng hay không.
     *
     * @param voucherId mã định danh voucher
     * @param request dữ liệu dùng để kiểm tra
     * @return kết quả kiểm tra và số tiền giảm dự kiến
     */
    @Override
    @Transactional(readOnly = true)
    public VoucherValidationResponseDTO validateVoucher(UUID voucherId, VoucherValidationRequestDTO request) {
        Voucher voucher = loadVoucher(voucherId);
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu kiểm tra voucher không được để trống");
        }

        String issue = findEligibilityIssue(voucher, request.getCustomerId(), request.getOrderAmount(), request.getShippingFee());
        boolean valid = issue == null;
        BigDecimal discountAmount = valid ? calculateDiscountAmount(voucher, request.getOrderAmount(), request.getShippingFee()) : BigDecimal.ZERO;

        return VoucherValidationResponseDTO.builder()
                .voucherId(voucher.getId())
                .code(voucher.getCode())
                .valid(valid)
                .message(valid ? "Voucher hợp lệ" : issue)
                .discountAmount(discountAmount)
                .remainingQuantity(voucher.getQuantity())
                .build();
    }

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
    @Override
    @Transactional
    public VoucherRedemptionResponseDTO redeemVoucher(UUID voucherId, UUID customerId, UUID orderId, BigDecimal orderAmount, BigDecimal shippingFee) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId không được để trống");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("orderId không được để trống");
        }

        Voucher voucher = loadVoucher(voucherId);
        String issue = findEligibilityIssue(voucher, customerId, orderAmount, shippingFee);
        if (issue != null) {
            throw new IllegalStateException(issue);
        }

        BigDecimal discountAmount = calculateDiscountAmount(voucher, orderAmount, shippingFee);
        Integer remainingQuantity = voucher.getQuantity() == null ? 0 : voucher.getQuantity() - 1;
        voucher.setQuantity(Math.max(remainingQuantity, 0));
        if (voucher.getQuantity() <= 0) {
            voucher.setStatus(VoucherStatus.EXPIRED);
        }
        voucherRepository.save(voucher);

        VoucherRedemption redemption = VoucherRedemption.builder()
                .voucher(voucher)
                .customerId(customerId)
                .orderId(orderId)
                .amountDiscounted(discountAmount)
                .redeemedAt(LocalDateTime.now())
                .build();

        return mapToRedemptionResponse(voucherRedemptionRepository.save(redemption));
    }

    /**
     * Lấy chi tiết một bản ghi lịch sử sử dụng voucher theo mã định danh.
     *
     * @param redemptionId mã định danh lịch sử sử dụng
     * @return bản ghi lịch sử tương ứng
     */
    @Override
    @Transactional(readOnly = true)
    public VoucherRedemptionResponseDTO getRedemptionById(UUID redemptionId) {
        return mapToRedemptionResponse(loadRedemption(redemptionId));
    }

    /**
     * Lấy danh sách lịch sử sử dụng theo voucher.
     *
     * @param voucherId mã định danh voucher
     * @return danh sách lịch sử áp dụng voucher
     */
    @Override
    @Transactional(readOnly = true)
    public List<VoucherRedemptionResponseDTO> getRedemptionsByVoucherId(UUID voucherId) {
        loadVoucher(voucherId);
        return voucherRedemptionRepository.findByVoucherIdOrderByRedeemedAtDesc(voucherId)
                .stream()
                .map(this::mapToRedemptionResponse)
                .toList();
    }

    /**
     * Lấy danh sách lịch sử sử dụng theo khách hàng.
     *
     * @param customerId mã định danh khách hàng
     * @return danh sách lịch sử sử dụng của khách hàng
     */
    @Override
    @Transactional(readOnly = true)
    public List<VoucherRedemptionResponseDTO> getRedemptionsByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId không được để trống");
        }
        return voucherRedemptionRepository.findByCustomerIdOrderByRedeemedAtDesc(customerId)
                .stream()
                .map(this::mapToRedemptionResponse)
                .toList();
    }

    /**
     * Tải voucher theo mã định danh và ném ngoại lệ nếu không tồn tại.
     *
     * @param voucherId mã định danh voucher
     * @return voucher tương ứng
     */
    private Voucher loadVoucher(UUID voucherId) {
        if (voucherId == null) {
            throw new IllegalArgumentException("voucherId không được để trống");
        }
        return voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException("Không tìm thấy voucher"));
    }

    /**
     * Tải voucher theo mã code và ném ngoại lệ nếu không tồn tại.
     *
     * @param code mã voucher cần tra cứu
     * @return voucher tương ứng
     */
    private Voucher loadVoucherByCode(String code) {
        String normalizedCode = normalizeCode(code);
        return voucherRepository.findByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new VoucherNotFoundException("Không tìm thấy voucher"));
    }

    /**
     * Tải lịch sử sử dụng voucher theo mã định danh.
     *
     * @param redemptionId mã định danh bản ghi lịch sử
     * @return bản ghi lịch sử tương ứng
     */
    private VoucherRedemption loadRedemption(UUID redemptionId) {
        if (redemptionId == null) {
            throw new IllegalArgumentException("redemptionId không được để trống");
        }
        return voucherRedemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new VoucherNotFoundException("Không tìm thấy lịch sử sử dụng voucher"));
    }

    /**
     * Kiểm tra dữ liệu voucher đầu vào trước khi lưu.
     *
     * @param request dữ liệu voucher cần kiểm tra
     */
    private void validateVoucherRequest(VoucherRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu voucher không được để trống");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Tên voucher không được để trống");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Loại voucher không được để trống");
        }
        if (request.getDiscountValue() == null) {
            throw new IllegalArgumentException("Giá trị giảm không được để trống");
        }
        if (request.getMinOrderAmount() == null) {
            throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu không được để trống");
        }
        if (request.getQuantity() == null) {
            throw new IllegalArgumentException("Số lượng voucher không được để trống");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Trạng thái voucher không được để trống");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày kết thúc không được để trống");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn ngày bắt đầu");
        }
    }

    /**
     * Chuẩn hóa mã voucher về dạng chữ in hoa không có khoảng trắng đầu cuối.
     *
     * @param code mã voucher gốc
     * @return mã voucher đã chuẩn hóa
     */
    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Chuẩn hóa số tiền theo quy tắc tiền tệ của hệ thống.
     *
     * @param value giá trị tiền cần chuẩn hóa
     * @return giá trị đã làm tròn về scale 0
     */
    private BigDecimal normalizeCurrency(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(0, CURRENCY_ROUNDING);
        }
        return value.setScale(0, CURRENCY_ROUNDING);
    }

    /**
     * Kiểm tra voucher có thể áp dụng hay không và trả về lý do nếu không hợp lệ.
     *
     * @param voucher voucher đang được kiểm tra
     * @param customerId mã định danh khách hàng
     * @param orderAmount giá trị đơn hàng
     * @param shippingFee phí vận chuyển gốc
     * @return null nếu hợp lệ, ngược lại là lý do không hợp lệ
     */
    private String findEligibilityIssue(Voucher voucher, UUID customerId, BigDecimal orderAmount, BigDecimal shippingFee) {
        if (voucher == null) {
            return "Không tìm thấy voucher";
        }

        if (voucher.getStatus() == VoucherStatus.DISABLED) {
            return "Voucher đã bị vô hiệu hóa";
        }
        if (voucher.getStatus() == VoucherStatus.UPCOMING) {
            return "Voucher chưa có hiệu lực";
        }
        if (voucher.getStatus() == VoucherStatus.EXPIRED) {
            return "Voucher đã hết hạn";
        }
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            return "Voucher không thể áp dụng";
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate())) {
            return "Voucher chưa đến thời gian bắt đầu";
        }
        if (now.isAfter(voucher.getEndDate())) {
            return "Voucher đã hết thời gian kết thúc";
        }
        if (voucher.getQuantity() == null || voucher.getQuantity() <= 0) {
            return "Voucher đã hết số lượng";
        }
        if (orderAmount == null) {
            return "Giá trị đơn hàng không được để trống";
        }

        BigDecimal normalizedOrderAmount = normalizeCurrency(orderAmount);
        BigDecimal normalizedMinOrderAmount = normalizeCurrency(voucher.getMinOrderAmount());
        if (normalizedOrderAmount.compareTo(normalizedMinOrderAmount) < 0) {
            return "Giá trị đơn hàng chưa đạt mức tối thiểu";
        }

        if (voucher.getMaxUsagePerUser() != null) {
            if (customerId == null) {
                return "customerId không được để trống";
            }
            long usageCount = voucherRedemptionRepository.countByVoucherIdAndCustomerId(voucher.getId(), customerId);
            if (usageCount >= voucher.getMaxUsagePerUser()) {
                return "Khách hàng đã vượt quá số lần sử dụng voucher";
            }
        }

        if (voucher.getType() == VoucherType.FREE_SHIPPING && shippingFee == null) {
            return "Phí vận chuyển không được để trống đối với voucher miễn phí vận chuyển";
        }

        return null;
    }

    /**
     * Tính số tiền giảm thực tế theo loại voucher.
     *
     * @param voucher voucher đang được áp dụng
     * @param orderAmount giá trị đơn hàng
     * @param shippingFee phí vận chuyển gốc
     * @return số tiền giảm thực tế
     */
    private BigDecimal calculateDiscountAmount(Voucher voucher, BigDecimal orderAmount, BigDecimal shippingFee) {
        BigDecimal normalizedOrderAmount = normalizeCurrency(orderAmount);
        BigDecimal discountAmount;

        if (voucher.getType() == VoucherType.PERCENT) {
            discountAmount = normalizedOrderAmount
                    .multiply(normalizeCurrency(voucher.getDiscountValue()))
                    .divide(BigDecimal.valueOf(100), 0, CURRENCY_ROUNDING);
            if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal normalizedMaxDiscountAmount = normalizeCurrency(voucher.getMaxDiscountAmount());
                if (discountAmount.compareTo(normalizedMaxDiscountAmount) > 0) {
                    discountAmount = normalizedMaxDiscountAmount;
                }
            }
        } else if (voucher.getType() == VoucherType.AMOUNT) {
            discountAmount = normalizeCurrency(voucher.getDiscountValue());
            if (discountAmount.compareTo(normalizedOrderAmount) > 0) {
                discountAmount = normalizedOrderAmount;
            }
        } else {
            BigDecimal normalizedShippingFee = shippingFee == null ? BigDecimal.ZERO : normalizeCurrency(shippingFee);
            discountAmount = normalizeCurrency(voucher.getDiscountValue());
            if (discountAmount.compareTo(normalizedShippingFee) > 0) {
                discountAmount = normalizedShippingFee;
            }
        }

        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(0, CURRENCY_ROUNDING);
        }
        return discountAmount.setScale(0, CURRENCY_ROUNDING);
    }

    /**
     * Ánh xạ thực thể voucher sang DTO phản hồi.
     *
     * @param voucher thực thể voucher
     * @return DTO phản hồi tương ứng
     */
    private VoucherResponseDTO mapToResponse(Voucher voucher) {
        return VoucherResponseDTO.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .type(voucher.getType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .minOrderAmount(voucher.getMinOrderAmount())
                .quantity(voucher.getQuantity())
                .maxUsagePerUser(voucher.getMaxUsagePerUser())
                .status(voucher.getStatus())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .createdAt(voucher.getCreatedAt())
                .build();
    }

    /**
     * Ánh xạ thực thể lịch sử sử dụng voucher sang DTO phản hồi.
     *
     * @param redemption thực thể lịch sử sử dụng voucher
     * @return DTO phản hồi tương ứng
     */
    private VoucherRedemptionResponseDTO mapToRedemptionResponse(VoucherRedemption redemption) {
        return VoucherRedemptionResponseDTO.builder()
                .id(redemption.getId())
                .voucherId(redemption.getVoucher() == null ? null : redemption.getVoucher().getId())
                .customerId(redemption.getCustomerId())
                .orderId(redemption.getOrderId())
                .amountDiscounted(redemption.getAmountDiscounted())
                .redeemedAt(redemption.getRedeemedAt())
                .build();
    }
}