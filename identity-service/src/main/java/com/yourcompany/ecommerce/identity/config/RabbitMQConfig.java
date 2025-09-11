package com.yourcompany.ecommerce.identity.config;

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

    public static final String EXCHANGE_NAME = "order_exchange";

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

    // === Cấu hình Message Converter VÀ RabbitTemplate ===
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}