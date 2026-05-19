package iuh.fit.orderservice.service.impl;

import iuh.fit.orderservice.client.CartServiceClient;
import iuh.fit.orderservice.client.CatalogServiceClient;
import iuh.fit.orderservice.dto.CartItemResponse;
import iuh.fit.orderservice.dto.CartResponse;
import iuh.fit.orderservice.dto.CreateOrderRequest;
import iuh.fit.orderservice.dto.OrderItemResponse;
import iuh.fit.orderservice.dto.OrderResponse;
import iuh.fit.orderservice.dto.ProductImageResponse;
import iuh.fit.orderservice.dto.ProductResponse;
import iuh.fit.orderservice.dto.ProductVariantResponse;
import iuh.fit.orderservice.dto.UpdateOrderStatusRequest;
import iuh.fit.orderservice.entity.Order;
import iuh.fit.orderservice.entity.OrderItem;
import iuh.fit.orderservice.entity.OrderStatus;
import iuh.fit.orderservice.entity.PaymentMethod;
import iuh.fit.orderservice.entity.PaymentStatus;
import iuh.fit.orderservice.event.OrderCreatedEvent;
import iuh.fit.orderservice.event.OrderEmailEvent;
import iuh.fit.orderservice.event.OrderEventPublisher;
import iuh.fit.orderservice.repo.OrderRepository;
import iuh.fit.orderservice.service.OrderService;
import iuh.fit.shared.error.BusinessException;
import iuh.fit.shared.error.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartServiceClient cartServiceClient,
            CatalogServiceClient catalogServiceClient,
            OrderEventPublisher orderEventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.cartServiceClient = cartServiceClient;
        this.catalogServiceClient = catalogServiceClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        validateCreateRequest(request);

        if (request.selectedItemIds() == null || request.selectedItemIds().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "No items selected for checkout");
        }

        CartResponse cart = cartServiceClient.getCart(request.customerId());
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cart is empty");
        }

        List<CartItemResponse> selectedItems = cart.items().stream()
                .filter(item -> request.selectedItemIds().contains(item.id().toString()))
                .toList();

        if (selectedItems.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Selected items not found in cart");
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomerId(UUID.fromString(request.customerId()));
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(resolvePaymentMethod(request.paymentMethod()));
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setRecipientName(request.recipientName());
        order.setShippingAddress(request.shippingAddress());
        order.setEmail(request.email());
        order.setPhone(request.phone());
        order.setNote(request.note());
        order.setOrderDate(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> items = buildOrderItems(order, selectedItems);
        order.setItems(items);

        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotal(subtotal);

        Order saved = orderRepository.save(order);

        orderEventPublisher.publishOrderCreated(buildOrderCreatedEvent(saved));
        orderEventPublisher.publishOrderEmail(buildOrderEmailEvent(saved));
        return mapToResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        UUID id = parseUuid(orderId, "Invalid orderId");
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Order not found"));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(String customerId) {
        UUID customerUuid = parseUuid(customerId, "Invalid customerId");
        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerUuid);
        return orders.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status is required");
        }

        UUID id = parseUuid(orderId, "Invalid orderId");
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Order not found"));

        OrderStatus newStatus = parseStatus(request.status());
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelReason(request.cancelReason());
            order.setCancelledAt(LocalDateTime.now());
        }

        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            if (order.getPaymentMethod() == PaymentMethod.COD) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }

        if (newStatus == OrderStatus.DELIVERY_FAILED){
            order.setCancelReason(request.cancelReason());
            order.setCancelledAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);

        orderEventPublisher.publishOrderEmail(buildOrderEmailEvent(saved));
        return mapToResponse(saved);
    }

    private void validateCreateRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Request body is required");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "customerId is required");
        }
        if (request.recipientName() == null || request.recipientName().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "recipientName is required");
        }
        if (request.shippingAddress() == null || request.shippingAddress().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "shippingAddress is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "email is required");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "phone is required");
        }
    }

    private List<OrderItem> buildOrderItems(Order order, List<CartItemResponse> cartItems) {
        List<OrderItem> items = new ArrayList<>();
        for (CartItemResponse cartItem : cartItems) {
            if (cartItem.productVariantId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Cart item missing productVariantId");
            }

            ProductVariantResponse variant = catalogServiceClient.getVariantById(cartItem.productVariantId());
            if (variant == null || variant.id() == null) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Product variant not found");
            }
            if (Boolean.FALSE.equals(variant.isActive())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Product variant is inactive");
            }
            if (variant.stockQuantity() != null && cartItem.quantity() > variant.stockQuantity()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Insufficient stock for product variant");
            }

            ProductResponse product = catalogServiceClient.getProductById(variant.productId());
            String productName = product == null ? null : product.name();
            String imageUrl = resolveImageUrl(product);

            BigDecimal unitPrice = variant.price() == null ? BigDecimal.ZERO : variant.price();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItem.quantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariantId(variant.id());
            orderItem.setProductName(productName);
            orderItem.setVariantName(variant.variantName());
            orderItem.setImageUrl(imageUrl == null ? variant.imageUrl() : imageUrl);
            orderItem.setQuantity(cartItem.quantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(totalPrice);
            items.add(orderItem);
        }
        return items;
    }

        private OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderCreatedItem> items = order.getItems() == null
            ? List.of()
            : order.getItems().stream()
            .map(item -> new OrderCreatedEvent.OrderCreatedItem(
                item.getProductVariantId(),
                item.getQuantity()
            ))
            .toList();

        return new OrderCreatedEvent(
            order.getId(),
            order.getCustomerId(),
            order.getOrderCode(),
            items
        );
        }

        private OrderEmailEvent buildOrderEmailEvent(Order order) {
        String updatedAt = order.getUpdatedAt() == null ? null : order.getUpdatedAt().toString();
        return new OrderEmailEvent(
            order.getEmail(),
            order.getRecipientName(),
            order.getOrderCode(),
            order.getStatus() == null ? null : order.getStatus().name(),
            order.getTotal(),
            updatedAt
        );
        }

    private String resolveImageUrl(ProductResponse product) {
        if (product == null || product.images() == null || product.images().isEmpty()) {
            return null;
        }
        for (ProductImageResponse image : product.images()) {
            if (Boolean.TRUE.equals(image.isPrimary())) {
                return image.url();
            }
        }
        return product.images().get(0).url();
    }

    private PaymentMethod resolvePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PaymentMethod.COD;
        }
        String normalized = paymentMethod.trim().toUpperCase(Locale.ROOT);
        try {
            return PaymentMethod.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid paymentMethod");
        }
    }

    private String generateOrderCode() {
        String datePart = LocalDateTime.now().format(ORDER_CODE_DATE);
        for (int attempt = 0; attempt < 5; attempt++) {
            int random = (int) (Math.random() * 9000) + 1000;
            String code = "ORD-" + datePart + "-" + random;
            if (!orderRepository.existsByOrderCode(code)) {
                return code;
            }
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to generate order code");
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid order status");
        }
    }

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductVariantId(),
                        item.getProductName(),
                        item.getVariantName(),
                        item.getImageUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotal(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getRecipientName(),
                order.getShippingAddress(),
                order.getEmail(),
                order.getPhone(),
                order.getNote(),
                order.getOrderDate(),
                order.getUpdatedAt(),
                order.getCancelReason(),
                items
        );
    }
}
