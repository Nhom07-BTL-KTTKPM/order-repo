package iuh.fit.orderservice.service;

import iuh.fit.orderservice.dto.CreateOrderRequest;
import iuh.fit.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(String orderId);

    List<OrderResponse> getOrdersByCustomerId(String customerId);
}
