package iuh.fit.orderservice.event;

import iuh.fit.orderservice.config.OrderEventProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OrderEventProperties properties;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate, OrderEventProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                properties.getOrderExchange(),
                properties.getRouting().getOrderCreated(),
                event
        );
    }

    public void publishOrderEmail(OrderEmailEvent event) {
        rabbitTemplate.convertAndSend(
                properties.getNotificationExchange(),
                properties.getRouting().getEmailOrder(),
                event
        );
    }
}
