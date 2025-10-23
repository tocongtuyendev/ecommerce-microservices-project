package com.yourcompany.ecommerce.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "order_exchange";

    // Queues
    public static final String INVENTORY_QUEUE_NAME = "inventory_queue";
    public static final String ORDER_QUEUE_NAME = "order_queue";

    // Routing Keys
    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";
    public static final String ROUTING_KEY_SUCCESS = "inventory.updated.success";
    public static final String ROUTING_KEY_FAILED = "inventory.updated.failed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // === Cấu hình cho inventory_queue ===
    @Bean
    public Queue inventoryQueue() {
        return new Queue(INVENTORY_QUEUE_NAME);
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, TopicExchange exchange) {
        return BindingBuilder.bind(inventoryQueue).to(exchange).with(ROUTING_KEY_ORDER_PLACED);
    }

    // === Cấu hình cho order_queue ===
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE_NAME);
    }

    @Bean
    public Binding successBinding(Queue orderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue).to(exchange).with(ROUTING_KEY_SUCCESS);
    }

    @Bean
    public Binding failedBinding(Queue orderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue).to(exchange).with(ROUTING_KEY_FAILED);
    }

    // === Cấu hình Message Converter ===
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}