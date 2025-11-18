package com.yourcompany.ecommerce.identity.config;

/**
 * RabbitMQ has been removed. Use {@link KafkaConfig} for topic names and
 * Kafka beans. This class remains deprecated temporarily for compatibility
 * and will be removed in a future commit.
 */
@Deprecated
public final class RabbitMQConfig {
    private RabbitMQConfig() {}
}