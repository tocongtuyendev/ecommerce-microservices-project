package com.yourcompany.ecommerce.seller.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order_exchange"; // Chúng ta có thể đổi tên này sau
    public static final String SELLER_QUEUE_NAME = "seller_management_queue";
    public static final String ROUTING_KEY_SELLER_REGISTERED = "seller.registered";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue sellerQueue() {
        return new Queue(SELLER_QUEUE_NAME);
    }

    @Bean
    public Binding sellerBinding(Queue sellerQueue, TopicExchange exchange) {
        return BindingBuilder.bind(sellerQueue).to(exchange).with(ROUTING_KEY_SELLER_REGISTERED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}