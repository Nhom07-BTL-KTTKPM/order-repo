package iuh.fit.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.rabbitmq")
public class OrderEventProperties {

    private String notificationExchange = "notification.exchange";
    private String orderExchange = "order.exchange";
    private Routing routing = new Routing();

    public String getNotificationExchange() {
        return notificationExchange;
    }

    public void setNotificationExchange(String notificationExchange) {
        this.notificationExchange = notificationExchange;
    }

    public String getOrderExchange() {
        return orderExchange;
    }

    public void setOrderExchange(String orderExchange) {
        this.orderExchange = orderExchange;
    }

    public Routing getRouting() {
        return routing;
    }

    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public static class Routing {
        private String emailOrder = "email.order";
        private String orderCreated = "order.created";

        public String getEmailOrder() {
            return emailOrder;
        }

        public void setEmailOrder(String emailOrder) {
            this.emailOrder = emailOrder;
        }

        public String getOrderCreated() {
            return orderCreated;
        }

        public void setOrderCreated(String orderCreated) {
            this.orderCreated = orderCreated;
        }
    }
}
