package iuh.fit.orderservice.service;

import iuh.fit.orderservice.dto.CreateGuestOrderRequest;
import iuh.fit.orderservice.dto.CreateOrderRequest;
import iuh.fit.orderservice.dto.OrderResponse;
import iuh.fit.orderservice.dto.UpdateOrderStatusRequest;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse createGuestOrder(CreateGuestOrderRequest request);

    OrderResponse getOrderById(String orderId);

    List<OrderResponse> getOrdersByCustomerId(String customerId);

    OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request);

    OrderResponse lookupOrder(String orderCode, String email);
}
