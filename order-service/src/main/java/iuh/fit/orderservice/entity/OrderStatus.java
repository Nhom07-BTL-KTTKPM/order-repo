package iuh.fit.orderservice.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    DELIVERY_FAILED
}
