package iuh.fit.orderservice.voucher.dto;

import iuh.fit.orderservice.voucher.entity.VoucherStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO dùng để thay đổi trạng thái voucher.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherStatusChangeRequestDTO {

    /** Trạng thái mới của voucher. */
    @NotNull(message = "Trạng thái mới không được để trống")
    private VoucherStatus status;
}