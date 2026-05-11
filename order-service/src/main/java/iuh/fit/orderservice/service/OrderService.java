package iuh.fit.orderservice.service;

import iuh.fit.orderservice.dto.CreateOrderRequest;
import iuh.fit.orderservice.dto.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
}
