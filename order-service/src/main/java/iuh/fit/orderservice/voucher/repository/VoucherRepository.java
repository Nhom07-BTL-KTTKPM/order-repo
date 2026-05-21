package iuh.fit.orderservice.voucher.repository;

import iuh.fit.orderservice.voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu cho thực thể Voucher.
 */
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    /**
     * Tìm voucher theo mã code bất kể chữ hoa hay chữ thường.
     *
     * @param code mã voucher cần tra cứu
     * @return thông tin voucher nếu tồn tại
     */
    Optional<Voucher> findByCodeIgnoreCase(String code);

    /**
     * Kiểm tra voucher có tồn tại với mã code hay không, loại trừ một id cụ thể.
     *
     * @param code mã voucher cần kiểm tra
     * @param id định danh voucher cần loại trừ khi cập nhật
     * @return true nếu đã tồn tại voucher khác cùng mã
     */
    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
}