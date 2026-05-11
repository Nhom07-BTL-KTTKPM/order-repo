package iuh.fit.orderservice.client;

import iuh.fit.orderservice.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service")
public interface CartServiceClient {

    @GetMapping("/api/v1/carts/{customerId}")
    CartResponse getCart(@PathVariable("customerId") String customerId);

    @DeleteMapping("/api/v1/carts/{customerId}")
    void clearCart(@PathVariable("customerId") String customerId);
}
