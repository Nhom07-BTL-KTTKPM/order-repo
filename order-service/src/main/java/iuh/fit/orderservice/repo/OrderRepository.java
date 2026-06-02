package iuh.fit.orderservice.repo;

import iuh.fit.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByOrderCode(String orderCode);

    List<Order> findByCustomerIdOrderByOrderDateDesc(UUID customerId);

    Optional<Order> findByOrderCodeAndEmail(String orderCode, String email);
}
